package org.cheetah.monitoring.repository;

import org.cheetah.monitoring.model.Alert;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertRepository extends MongoRepository<Alert, String> {
	/**
	 * Returns the most recent alert for the given ip, metric type, and port.
	 */
	Alert findTopByIpAndMetricTypeAndPortOrderByTimestampDesc(String ip, String metricType, int port);

	/**
	 * Returns the most recent alert for the given ip, metric type.
	 */
	Alert findTopByIpAndMetricTypeOrderByTimestampDesc(String ip, String metricType);
}