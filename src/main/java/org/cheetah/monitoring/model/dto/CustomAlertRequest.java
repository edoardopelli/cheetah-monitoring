// src/main/java/org/cheetah/monitoring/dto/CustomAlertRequest.java
package org.cheetah.monitoring.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body to send a custom external alert.
 */
@Data
public class CustomAlertRequest {
    @NotBlank(message = "appName is required")
    private String appName;

    @NotBlank(message = "apiKey is required")
    private String apiKey;

    @NotBlank(message = "executionDate is required")
    private String executionDate;

    @NotBlank(message = "application is required")
    private String application;

    /** optional textual log of the operation */
    private String log;
}