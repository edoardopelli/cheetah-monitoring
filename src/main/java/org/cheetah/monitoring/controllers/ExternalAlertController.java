// src/main/java/org/cheetah/monitoring/controller/ExternalAlertController.java
package org.cheetah.monitoring.controllers;

import org.cheetah.monitoring.model.dto.CreateApiKeyRequest;
import org.cheetah.monitoring.model.dto.CustomAlertRequest;
import org.cheetah.monitoring.services.AlertService;
import org.cheetah.monitoring.services.ApiClientService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

/**
 * Exposes endpoints for external systems to send custom Telegram alerts.
 */
@RestController
@RequestMapping("/api/external")
public class ExternalAlertController {

	private final ApiClientService clientService;
	private final AlertService alertService;

	public ExternalAlertController(ApiClientService clientService, AlertService alertService) {
		this.clientService = clientService;
		this.alertService = alertService;
	}

	/**
	 * Generate a new API key for an external application.
	 */
	@PostMapping("/apikey")
	public ResponseEntity<?> createApiKey(@Valid @RequestBody CreateApiKeyRequest req) {
		var client = clientService.createClient(req.getAppName());
		if (client == null) {
			return ResponseEntity.badRequest().body("appName already exists");
		}
		return ResponseEntity.ok(client);
	}

	/**
	 * Accepts custom alert requests from external systems.
	 */
	@PostMapping("/alert")
	public ResponseEntity<?> sendCustomAlert(@Valid @RequestBody CustomAlertRequest req) {
		if (!clientService.validateKey(req.getAppName(), req.getApiKey())) {
			return ResponseEntity.status(401).body("Invalid apiKey or appName");
		}

		String message = String.format("[%s] \nApplication: %s\nDate: %s\nLog:\n%s", req.getAppName(),
				req.getApplication(), req.getExecutionDate(),
				req.getLog() != null ? req.getLog() : "(no log provided)");

		alertService.sendCustomTelegramAlert("EXTERNAL", message);
		return ResponseEntity.ok("Alert sent");
	}

}