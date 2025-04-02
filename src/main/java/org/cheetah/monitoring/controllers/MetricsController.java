package org.cheetah.monitoring.controllers;

import org.cheetah.monitoring.model.Metrics;
import org.cheetah.monitoring.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to handle incoming metrics from remote agents.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    @Autowired
    private MetricsService metricsService;

    /**
     * Receives metrics data via HTTP POST.
     * @param metrics Metrics data in JSON format.
     */
    @PostMapping
    public void receiveMetrics(@RequestBody Metrics metrics) {
        metricsService.saveMetrics(metrics);
    }
}