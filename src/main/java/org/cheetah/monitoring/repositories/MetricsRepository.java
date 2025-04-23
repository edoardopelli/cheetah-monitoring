package org.cheetah.monitoring.repositories;

import org.cheetah.monitoring.model.Metrics;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for Metrics operations.
 */
public interface MetricsRepository extends MongoRepository<Metrics, String> {
    // Add custom query methods if necessary
	/**
     * Retrieves the latest metrics record for a given hostname.
     */
    Metrics findTopByHostnameOrderByTimestampDesc(String hostname);
}