package org.cheetah.monitoring.repository;

import org.cheetah.monitoring.model.Metrics;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for Metrics operations.
 */
public interface MetricsRepository extends MongoRepository<Metrics, String> {
    // Add custom query methods if necessary
}