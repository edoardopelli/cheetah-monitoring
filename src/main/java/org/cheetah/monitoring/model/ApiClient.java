// src/main/java/org/cheetah/monitoring/model/ApiClient.java
package org.cheetah.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents an external application client authorized to send custom alerts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "apiClients")
public class ApiClient {
    @Id
    private String id;

    /** Unique name of the external application */
    private String appName;

    /** Generated API key for authentication */
    private String apiKey;

    /** Creation timestamp (milliseconds since epoch) */
    private long createdAt;

    /** Human‐readable creation date */
    private String createdAtHuman;
}