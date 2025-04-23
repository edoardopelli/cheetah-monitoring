package org.cheetah.monitoring.services;

import java.util.List;

import org.cheetah.monitoring.model.Threshold;
import org.cheetah.monitoring.repositories.ThresholdRepository;
import org.springframework.stereotype.Service;

@Service
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;

    public ThresholdService(ThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    /**
     * Restituisce tutte le configurazioni di soglia.
     */
    public List<Threshold> getAllThresholds() {
        return thresholdRepository.findAll();
    }

    /**
     * Restituisce la soglia per il tipo di metrica specificato.
     */
    public Threshold getThresholdByMetricType(String metricType) {
        return thresholdRepository.findByMetricType(metricType);
    }

    /**
     * Crea una nuova configurazione di soglia.
     * Se una soglia per il tipo specificato esiste già, viene ritornato null.
     */
    public Threshold createThreshold(Threshold threshold) {
        Threshold existing = thresholdRepository.findByMetricType(threshold.getMetricType());
        if (existing != null) {
            return null;
        }
        return thresholdRepository.save(threshold);
    }

    /**
     * Aggiorna la configurazione della soglia per il tipo di metrica specificato.
     */
    public Threshold updateThreshold(String metricType, Threshold thresholdUpdate) {
        Threshold existing = thresholdRepository.findByMetricType(metricType);
        if (existing == null) {
            return null;
        }
        existing.setThresholdValue(thresholdUpdate.getThresholdValue());
        return thresholdRepository.save(existing);
    }

    /**
     * Elimina la configurazione della soglia per il tipo di metrica specificato.
     */
    public boolean deleteThreshold(String metricType) {
        Threshold existing = thresholdRepository.findByMetricType(metricType);
        if (existing == null) {
            return false;
        }
        thresholdRepository.delete(existing);
        return true;
    }
}