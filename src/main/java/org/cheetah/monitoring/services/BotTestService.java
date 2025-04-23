package org.cheetah.monitoring.services;

import org.springframework.stereotype.Service;

@Service
public class BotTestService {

    private final AlertService alertService;

    public BotTestService(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Sends a test message through the Telegram bot to verify connectivity and configuration.
     *
     * @param message The test message to send.
     */
    public void testBot(String message) {
        // You can set a custom metricType "TEST" to differentiate test alerts.
        alertService.sendCustomTelegramAlert("TEST", message);
    }
}