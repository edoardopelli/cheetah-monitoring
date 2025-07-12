// src/main/java/org/cheetah/monitoring/service/ApiClientService.java
package org.cheetah.monitoring.services;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.cheetah.monitoring.model.ApiClient;
import org.cheetah.monitoring.repositories.ApiClientRepository;
import org.springframework.stereotype.Service;

/**
 * Service for managing external API clients and API key validation.
 */
@Service
public class ApiClientService {

    private final ApiClientRepository clientRepo;
    private static final DateTimeFormatter HUMAN_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                         .withZone(ZoneId.systemDefault());

    public ApiClientService(ApiClientRepository clientRepo) {
        this.clientRepo = clientRepo;
    }

    /**
     * Creates a new API client for the given application name.
     * Generates a unique API key and stores it in MongoDB.
     * Returns the created client, or null if the appName already exists.
     */
    public ApiClient createClient(String appName) {
        if (clientRepo.findByAppName(appName) != null) {
            return null; // appName already exists
        }
        long now = Instant.now().toEpochMilli();
        String human = HUMAN_FORMATTER.format(Instant.ofEpochMilli(now));

        ApiClient client = ApiClient.builder()
                .appName(appName)
                .apiKey(UUID.randomUUID().toString())
                .createdAt(now)
                .createdAtHuman(human)
                .build();
        return clientRepo.save(client);
    }

    /**
     * Validates that the given API key is associated with the given appName.
     */
    public boolean validateKey(String appName, String apiKey) {
        ApiClient client = clientRepo.findByAppName(appName);
        return client != null && client.getApiKey().equals(apiKey);
    }
}