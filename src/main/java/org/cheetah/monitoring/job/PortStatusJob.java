package org.cheetah.monitoring.job;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.cheetah.monitoring.model.AgentInfo;
import org.cheetah.monitoring.model.Alert;
import org.cheetah.monitoring.model.JobLock;
import org.cheetah.monitoring.repositories.AgentInfoRepository;
import org.cheetah.monitoring.repositories.AlertRepository;
import org.cheetah.monitoring.repositories.JobLockRepository;
import org.cheetah.monitoring.services.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PortStatusJob {

    // Static final UUID, computed once when the class is loaded.
    private static final String JOB_UUID = UUID.randomUUID().toString();

    @Autowired
    private AgentInfoRepository agentInfoRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private JobLockRepository jobLockRepository;

    @Autowired
    private AlertService alertService;

    /**
     * Scheduled job that runs every 5 minutes.
     * It checks for a lock based on the static JOB_UUID.
     * If the lock exists, the job skips execution.
     * Otherwise, it creates the lock, verifies each agent's ports and the agent's own listening port,
     * sends alerts via Telegram if necessary (only once every 24 hours per issue),
     * updates the agent's status accordingly, and finally removes the lock.
     */
    @Scheduled(fixedDelay = 300000)
    public void checkPortsStatus() {
        // Check for an existing lock using JOB_UUID.
        Optional<JobLock> lockOpt = jobLockRepository.findById(JOB_UUID);
        if (lockOpt.isPresent()) {
            System.out.println("Job lock exists with UUID " + JOB_UUID + ", skipping execution.");
            return;
        }
        // Create and save the lock.
        JobLock jobLock = JobLock.builder()
                .id(JOB_UUID)
                .jobName("PortStatusJob")
                .lockId(JOB_UUID)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        jobLockRepository.save(jobLock);
        System.out.println("Job lock created with UUID " + JOB_UUID);

        try {
            long twentyFourHoursAgo = Instant.now().minusSeconds(24 * 3600).toEpochMilli();

            // Retrieve all registered agents.
            List<AgentInfo> agents = agentInfoRepository.findAll();
            for (AgentInfo agent : agents) {
                String ip = agent.getIp();
                String hostname = agent.getHostname();
                
                // --- Check if the agent itself is reachable on agentPort ---
                int agentPort = agent.getAgentPort();
                if (!isPortOpen(ip, agentPort)) {
                    // Agent is not reachable, update status if not already "DOWN".
                    if (!"DOWN".equalsIgnoreCase(agent.getStatus())) {
                        agent.setStatus("DOWN");
                        agentInfoRepository.save(agent);
                    }
                    // Check for an "AGENT" alert in the last 24 hours.
                    Alert lastAgentAlert = alertRepository.findTopByIpAndMetricTypeOrderByTimestampDesc(ip, "AGENT");
                    if (lastAgentAlert == null || lastAgentAlert.getTimestamp() < twentyFourHoursAgo) {
                        String message = String.format("Alert! Agent %s (%s) is down on port %d.", hostname, ip, agentPort);
                        alertService.sendCustomTelegramAlert("AGENT", message);
                        Alert alert = Alert.builder()
                                .hostname(hostname)
                                .ip(ip).date(new Date())
                                .metricType("AGENT")
                                .port(null) // Not applicable for agent-down alert.
                                .timestamp(Instant.now().toEpochMilli())
                                .build();
                        alertRepository.save(alert);
                    }
                } else {
                    // Agent is reachable; ensure status is "UP".
                    if (!"UP".equalsIgnoreCase(agent.getStatus())) {
                        agent.setStatus("UP");
                        agentInfoRepository.save(agent);
                    }
                }

                // --- Check the reported open ports (PORT alerts) ---
                List<Integer> ports = agent.getOpenPorts();
                if (ports == null || ports.isEmpty()) {
                    continue;
                }
                for (Integer port : ports) {
                    if (!isPortOpen(ip, port)) {
                        // Retrieve the most recent alert for this IP, metric type "PORT", and port.
                        Alert lastPortAlert = alertRepository.findTopByIpAndMetricTypeAndPortOrderByTimestampDesc(ip, "PORT", port);
                        if (lastPortAlert == null || lastPortAlert.getTimestamp() < twentyFourHoursAgo) {
                            String message = String.format("Alert! %s (%s): port %d is down.", hostname, ip, port);
                            alertService.sendCustomTelegramAlert("PORT", message);
                            Alert alert = Alert.builder()
                                    .hostname(hostname)
                                    .ip(ip)
                                    .metricType("PORT")
                                    .port(port)
                                    .timestamp(Instant.now().toEpochMilli())
                                    .build();
                            alertRepository.save(alert);
                        }
                    }
                }
            }
        } finally {
            // Remove the lock at the end of job execution.
            jobLockRepository.deleteById(JOB_UUID);
            System.out.println("Job lock with UUID " + JOB_UUID + " removed.");
        }
    }

    /**
     * Checks if a specific port on a given IP is open by attempting a TCP connection with a timeout.
     *
     * @param ip   The IP address.
     * @param port The port number.
     * @return true if the port is open, false otherwise.
     */
    private boolean isPortOpen(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 200);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}