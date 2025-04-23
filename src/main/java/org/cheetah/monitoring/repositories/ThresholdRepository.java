package org.cheetah.monitoring.repositories;

import org.cheetah.monitoring.model.Threshold;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThresholdRepository extends MongoRepository<Threshold, String> {
    Threshold findByMetricType(String metricType);
}