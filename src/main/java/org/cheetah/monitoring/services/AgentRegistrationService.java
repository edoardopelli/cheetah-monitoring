package org.cheetah.monitoring.services;

import org.cheetah.monitoring.model.AgentInfo;
import org.cheetah.monitoring.repositories.AgentInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentRegistrationService {

    @Autowired
    private AgentInfoRepository agentInfoRepository;

    /**
     * Registers or updates an agent's information.
     * If an agent with the same IP and hostname already exists, its information is updated.
     *
     * @param agentInfo The registration data sent by the agent.
     * @return The saved or updated AgentInfo object.
     */
    public AgentInfo registerAgent(AgentInfo agentInfo) {
        AgentInfo existingAgent = agentInfoRepository.findByIpAndHostname(agentInfo.getIp(), agentInfo.getHostname());
        if (existingAgent != null) {
            // Update existing record
            existingAgent.setOpenPorts(agentInfo.getOpenPorts());
            existingAgent.setTimestamp(agentInfo.getTimestamp());
            existingAgent.setAgentPort(agentInfo.getAgentPort());
            return agentInfoRepository.save(existingAgent);
        } else {
            // Create new record
            return agentInfoRepository.save(agentInfo);
        }
    }
}