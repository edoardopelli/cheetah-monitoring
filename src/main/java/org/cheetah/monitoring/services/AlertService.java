package org.cheetah.monitoring.services;

import java.time.Instant;
import java.util.Date;

import org.cheetah.monitoring.model.Alert;
import org.cheetah.monitoring.model.Metrics;
import org.cheetah.monitoring.model.Threshold;
import org.cheetah.monitoring.repositories.AlertRepository;
import org.cheetah.monitoring.repositories.ThresholdRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AlertService {

    @Value("${telegram.bot.token}")
    private String telegramBotToken;

    @Value("${telegram.chat.id}")
    private String telegramChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    private final AlertRepository alertRepository;

	private final ThresholdRepository thresholdRepository;

    public AlertService(AlertRepository alertRepository,ThresholdRepository thresholdRepository) {
        this.alertRepository = alertRepository;
		this.thresholdRepository = thresholdRepository;
    }

    /**
     * Checks each metric (CPU, Disk, RAM) and sends an alert via Telegram if a threshold is exceeded.
     * For each IP and metric type, an alert is sent only if no alert has been sent in the last 24 hours.
     *
     * @param metrics The metrics data to check.
     */
    public void checkAndSendAlerts(Metrics metrics) {
        // Retrieve thresholds from database. If not present, use defaults.
        Threshold cpuThreshold = thresholdRepository.findByMetricType("CPU");
        double cpuLimit = cpuThreshold != null ? cpuThreshold.getThresholdValue() : 95.0;
        if (metrics.getCpuUsage() >= cpuLimit) {
            checkAndSendAlertForMetric("CPU", metrics);
        }

        Threshold diskThreshold = thresholdRepository.findByMetricType("Disk");
        double diskLimit = diskThreshold != null ? diskThreshold.getThresholdValue() : 85.0;
        if (metrics.getDiskUsage() >= diskLimit) {
            checkAndSendAlertForMetric("Disk", metrics);
        }

        Threshold ramThreshold = thresholdRepository.findByMetricType("RAM");
        double ramLimit = ramThreshold != null ? ramThreshold.getThresholdValue() : 80.0;
        if (metrics.getRamUsage() >= ramLimit) {
            checkAndSendAlertForMetric("RAM", metrics);
        }
    }

    /**
     * Checks and sends an alert for the specified metric type if the threshold is exceeded,
     * ensuring that no alert has been sent in the last 24 hours for the same IP and metric type.
     *
     * @param metricType The metric type (e.g., "CPU", "Disk", "RAM").
     * @param metrics    The metrics data.
     */
    private void checkAndSendAlertForMetric(String metricType, Metrics metrics) {
        // Calculate timestamp for 24 hours ago
        long twentyFourHoursAgo = Instant.now().minusSeconds(24 * 3600).toEpochMilli();

        // Retrieve the most recent alert for the given IP and metric type
        Alert lastAlert = alertRepository.findTopByIpAndMetricTypeOrderByTimestampDesc(metrics.getIp(), metricType);
        if (lastAlert != null && lastAlert.getTimestamp() > twentyFourHoursAgo) {
            // An alert has already been sent within the last 24 hours; do not send a new one.
            return;
        }

        // Get the metric value for the given metric type
        double metricValue = getMetricValue(metricType, metrics);

        // Compose the alert message
        String message = String.format("Alert! %s (%s) has high %s usage: %.2f%%",
                metrics.getHostname(), metrics.getIp(), metricType, metricValue);

        // Send the alert via Telegram
        sendCustomTelegramAlert(metricType, message);

        // Record the alert in the database
        Alert alertRecord = Alert.builder()
                .hostname(metrics.getHostname())
                .ip(metrics.getIp())
                .date(new Date())
                .metricType(metricType)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        alertRepository.save(alertRecord);
    }

    /**
     * Returns the value of the specified metric from the metrics data.
     *
     * @param metricType The metric type (e.g., "CPU", "Disk", "RAM").
     * @param metrics    The metrics data.
     * @return The metric value.
     */
    private double getMetricValue(String metricType, Metrics metrics) {
        switch (metricType) {
            case "CPU":
                return metrics.getCpuUsage();
            case "Disk":
                return metrics.getDiskUsage();
            case "RAM":
                return metrics.getRamUsage();
            default:
                return 0.0;
        }
    }

    /**
     * Sends a custom Telegram alert with the specified message.
     *
     * @param metricType The type of metric for which the alert is being sent.
     * @param message    The alert message.
     */
    public void sendCustomTelegramAlert(String metricType, String message) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", telegramBotToken);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", telegramChatId);
        params.add("text", message);
        try {
            restTemplate.postForObject(url, params, String.class);
        } catch (Exception e) {
            System.out.println("Error sending telegram alert: " + e.getMessage());
        }
    }
}