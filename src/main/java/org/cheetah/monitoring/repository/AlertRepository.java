package org.cheetah.monitoring.repository;

import org.cheetah.monitoring.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertRepository extends MongoRepository<Alert, String> {
    /**
     * Returns the most recent alert for the given ip and metricType.
     */
    Alert findTopByIpAndMetricTypeOrderByTimestampDesc(String ip, String metricType);
}