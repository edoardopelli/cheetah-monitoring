// src/main/java/org/cheetah/monitoring/repository/ApiClientRepository.java
package org.cheetah.monitoring.repositories;

import org.cheetah.monitoring.model.ApiClient;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for CRUD operations on ApiClient documents.
 */
public interface ApiClientRepository extends MongoRepository<ApiClient, String> {
    /** Find a client by its application name. */
    ApiClient findByAppName(String appName);

    /** Find a client by its API key. */
    ApiClient findByApiKey(String apiKey);
}