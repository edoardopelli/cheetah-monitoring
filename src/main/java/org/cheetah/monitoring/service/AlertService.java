package org.cheetah.monitoring.service;

import java.time.Instant;

import org.cheetah.monitoring.model.Alert;
import org.cheetah.monitoring.model.Metrics;
import org.cheetah.monitoring.repository.AlertRepository;
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

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Checks each metric in the provided Metrics object and sends an alert via Telegram if a threshold is exceeded.
     * For each ip-metricType pair, only a new alert is sent if none has been sent in the last 24 hours.
     * @param metrics Metrics data to check.
     */
    public void checkAndSendAlerts(Metrics metrics) {
        if (metrics.getCpuUsage() >= 95.0) {
            checkAndSendAlertForMetric("CPU", metrics);
        }
        if (metrics.getDiskUsage() >= 85.0) {
            checkAndSendAlertForMetric("Disk", metrics);
        }
        if (metrics.getRamUsage() >= 80.0) {
            checkAndSendAlertForMetric("RAM", metrics);
        }
    }

    private void checkAndSendAlertForMetric(String metricType, Metrics metrics) {
        // Ottieni l'ultimo alert per la coppia ip-metricType
        Alert lastAlert = alertRepository.findTopByIpAndMetricTypeOrderByTimestampDesc(metrics.getIp(), metricType);
        long twentyFourHoursAgo = Instant.now().minusSeconds(24 * 3600).toEpochMilli();
        if (lastAlert != null && lastAlert.getTimestamp() > twentyFourHoursAgo) {
            // Se l'ultimo alert è stato inviato meno di 24 ore fa, non inviare un nuovo alert
            return;
        }
        // Invia l'alert via Telegram
        sendTelegramAlert(metricType, metrics);
        // Registra l'invio dell'alert nella collection "alerts"
        Alert alertRecord = Alert.builder()
                .hostname(metrics.getHostname())
                .ip(metrics.getIp())
                .metricType(metricType)
                .timestamp(Instant.now().toEpochMilli())
                .build();
        alertRepository.save(alertRecord);
    }

    /**
     * Sends an alert message via Telegram for the specified metric.
     * @param metricType The type of metric ("CPU", "Disk", or "RAM").
     * @param metrics The metrics data.
     */
    private void sendTelegramAlert(String metricType, Metrics metrics) {
        String message = String.format("Alert! %s (%s) has high %s usage: %.2f%%",
                metrics.getHostname(), metrics.getIp(), metricType, getMetricValue(metricType, metrics));
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

    /**
     * Returns the value of the specified metric.
     * @param metricType The type of metric ("CPU", "Disk", or "RAM").
     * @param metrics The metrics data.
     * @return The percentage value for the metric.
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
}