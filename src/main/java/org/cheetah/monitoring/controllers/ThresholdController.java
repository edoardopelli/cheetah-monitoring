package org.cheetah.monitoring.controllers;

import java.util.List;

import org.cheetah.monitoring.model.Threshold;
import org.cheetah.monitoring.services.ThresholdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/thresholds")
public class ThresholdController {

    private final ThresholdService thresholdService;

    public ThresholdController(ThresholdService thresholdService) {
        this.thresholdService = thresholdService;
    }

    /**
     * GET /api/thresholds
     * Restituisce tutte le configurazioni di soglia.
     */
    @GetMapping
    public ResponseEntity<List<Threshold>> getAllThresholds() {
        List<Threshold> thresholds = thresholdService.getAllThresholds();
        return ResponseEntity.ok(thresholds);
    }

    /**
     * GET /api/thresholds/{metricType}
     * Restituisce la soglia per il tipo di metrica specificato.
     */
    @GetMapping("/{metricType}")
    public ResponseEntity<Threshold> getThresholdByMetricType(@PathVariable String metricType) {
        Threshold threshold = thresholdService.getThresholdByMetricType(metricType);
        if (threshold == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(threshold);
    }

    /**
     * POST /api/thresholds
     * Crea una nuova configurazione di soglia.
     * Se esiste già una soglia per il tipo specificato, ritorna un errore.
     */
    @PostMapping
    public ResponseEntity<Threshold> createThreshold(@RequestBody Threshold threshold) {
        Threshold created = thresholdService.createThreshold(threshold);
        if (created == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /api/thresholds/{metricType}
     * Aggiorna la configurazione di soglia per il tipo specificato.
     */
    @PutMapping("/{metricType}")
    public ResponseEntity<Threshold> updateThreshold(@PathVariable String metricType,
                                                     @RequestBody Threshold thresholdUpdate) {
        Threshold updated = thresholdService.updateThreshold(metricType, thresholdUpdate);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/thresholds/{metricType}
     * Elimina la configurazione di soglia per il tipo specificato.
     */
    @DeleteMapping("/{metricType}")
    public ResponseEntity<Void> deleteThreshold(@PathVariable String metricType) {
        boolean deleted = thresholdService.deleteThreshold(metricType);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}