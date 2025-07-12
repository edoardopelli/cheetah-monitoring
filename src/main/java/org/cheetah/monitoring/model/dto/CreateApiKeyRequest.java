// src/main/java/org/cheetah/monitoring/dto/CreateApiKeyRequest.java
package org.cheetah.monitoring.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body to generate a new API key.
 */
@Data
public class CreateApiKeyRequest {
    @NotBlank(message = "appName is required")
    private String appName;
}