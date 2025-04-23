package org.cheetah.monitoring.services;

import org.cheetah.monitoring.model.Metrics;
import org.cheetah.monitoring.repositories.MetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling business logic related to metrics.
 */
@Service
public class MetricsService {

    @Autowired
    private MetricsRepository metricsRepository;
    
    @Autowired
    private AlertService alertService;


    /**
     * Saves the received metrics into MongoDB.
     * @param metrics Metrics object received from the agent.
     */
    public void saveMetrics(Metrics metrics) {
        metricsRepository.save(metrics);
        // Check if any metric has reached the critical threshold
        alertService.checkAndSendAlerts(metrics);    }
}