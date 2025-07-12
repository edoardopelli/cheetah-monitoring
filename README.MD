# Monitoring Server Documentation

This document describes the functionality and architecture of the monitoring server, which is responsible for receiving data from remote agents, storing and processing metrics, sending alerts via Telegram, performing additional health checks on registered agents, and exposing a flexible API for external custom alerts.

---

## Architecture Overview

The server is implemented as a Spring Boot application and uses MongoDB as its data store. It provides the following key functionalities:

### 1. Agent Registration

Agents register themselves by sending their information to the endpoint `/api/agent/register`.

- **Data Storage:**  
  The registration payload includes the following attributes and is stored in the `agents` collection. If an agent with the same IP and hostname already exists, the record is updated.  
  - **Hostname**  
  - **IP Address**  
  - **List of Open Ports** (provided via the `PORTS` environment variable or determined by scanning)  
  - **Timestamp**  
  - **AgentPort** – the port on which the agent is listening  
  *Note: The server manages the agent’s status (“UP” or “DOWN”).*

### 2. Metrics Reception

Agents periodically send system metrics to the endpoint `/api/metrics`.

- **Data Storage:**  
  Metrics (hostname, IP, timestamp, CPU usage, disk usage, and RAM usage) are stored in the `metrics` collection within the `telemetrydb` database.  
- **Alert Checking:**  
  After saving, the server compares each metric against thresholds. If a threshold is exceeded, an alert is sent via Telegram and recorded in the `alerts` collection.  
  Duplicate notifications are avoided – for each IP–metricType combination, alerts are sent at most once every 24 hours.

### 3. Dynamic Threshold Management

Thresholds for critical metrics (e.g., CPU, Disk, RAM, and optionally PORT) are stored in a dedicated collection named `thresholds`. These thresholds are loaded dynamically so that changes in thresholds can be applied without modifying code.

- **Default Thresholds:** If a threshold is not found in the database, default values are used:  
  - **CPU:** 95%  
  - **Disk:** 85%  
  - **RAM:** 80%  
- **Threshold Management API:**  
  You can manage thresholds via REST endpoints (see below).

### 4. Scheduled Port and Agent Status Check

A scheduled job (`PortStatusJob`) runs every 5 minutes to verify agent health:

- **Port Verification:**  
  For each agent, the job iterates through the list of open ports and uses a TCP connection attempt to verify if the port is reachable. If a port is down and no alert (metric type “PORT”) has been sent in the last 24 hours for that IP and port, an alert is sent via Telegram and recorded.  
- **Agent Reachability:**  
  The job also verifies if the agent is reachable on its `agentPort`. If not, the agent’s status is updated to “DOWN” and an “AGENT” alert is sent (once every 24 hours for a given issue).  
- **Lock Mechanism:**  
  To prevent concurrent or redundant executions, the job uses a persistent lock stored in the `jobLocks` collection with a static UUID. Only one instance of the job runs per cycle.

### 5. Test Bot API

- **Endpoint:** `/api/bottest`  
- **Purpose:**  
  Allows you to send a test message via the Telegram bot to verify connectivity and configuration.  
- **Method:** GET  
- **Usage Example:**  
  `GET /api/bottest?message=Hello+World`

### 6. Threshold Management API

- `GET /api/thresholds` — Retrieve all threshold configurations.  
- `GET /api/thresholds/{metricType}` — Retrieve a threshold for a specific metric.  
- `POST /api/thresholds` — Create a new threshold configuration.  
- `PUT /api/thresholds/{metricType}` — Update an existing threshold configuration.  
- `DELETE /api/thresholds/{metricType}` — Delete a threshold configuration.

### 7. External Custom Alerts API

Allows external systems to send custom alerts to Telegram using an API key:

- **Generate API Key**  
  - **Endpoint:** `POST /api/external/apikey`  
  - **Request Body:**  
    ```json
    { "appName": "myBackupJob" }
    ```  
  - **Response:**  
    ```json
    {
      "id": "...",
      "appName": "myBackupJob",
      "apiKey": "...",
      "createdAt": 168...,  
      "createdAtHuman": "2025-07-12 14:23:10"
    }
    ```
- **Send Custom Alert**  
  - **Endpoint:** `POST /api/external/alert`  
  - **Request Body:**  
    ```json
    {
      "appName": "myBackupJob",
      "apiKey": "...",
      "executionDate": "2025-07-12T02:00:00Z",
      "application": "DatabaseBackup",
      "log": "Backup completed successfully"
    }
    ```  
  - **Behavior:**  
    - Validates `appName` + `apiKey`.  
    - Sends a Telegram message with the provided details under metric type “EXTERNAL”.

---

## Components and Data Models

### ApiClient Model

```java
public class ApiClient {
    private String id;
    private String appName;
    private String apiKey;
    private long createdAt;
    private String createdAtHuman;
}
```

### CreateApiKeyRequest DTO

```java
public class CreateApiKeyRequest {
    @NotBlank
    private String appName;
}
```

### CustomAlertRequest DTO

```java
public class CustomAlertRequest {
    @NotBlank private String appName;
    @NotBlank private String apiKey;
    @NotBlank private String executionDate;
    @NotBlank private String application;
    private String log;
}
```

---

## Configuration

### MongoDB Collections

- `agents`, `metrics`, `alerts`, `jobLocks`, `thresholds`, `apiClients`

### Swagger / OpenAPI

Add to `pom.xml`:

```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.1.0</version>
</dependency>
```

Once running, access:  
- **OpenAPI JSON:** `GET /v3/api-docs`  
- **Swagger UI:** `/swagger-ui.html`

---

## Deployment and Build

### Environment Variables (Agent)

- `PORTS`, `MONITORING_SERVER_HOST`, `MONITORING_SERVER_PORT`, `SEND_INTERVAL`

### Telegram Properties

```properties
telegram.bot.token=YOUR_TOKEN
telegram.chat.id=YOUR_CHAT_ID
telegram.webhook.path=/api/telegram/webhook
telegram.commands.status.description=Get current metrics for a host.
telegram.commands.ports.description=Get open ports for a host.
telegram.commands.list.description=List all monitored hosts.
telegram.commands.help.description=Show this help message.
```

### GitHub Actions for Maven Release

Workflow triggers on `push` to `main`, skips if commit message contains `[maven-release-plugin]`, uses Java 21, SSH agent, and deploys to GitHub Packages.

---

## Summary

- **Registration & Metrics:** Agents register and send metrics.  
- **Dynamic Thresholds:** Managed via MongoDB with REST API.  
- **Port & Agent Health:** Scheduled checks with alert suppression.  
- **Telegram Bot Commands:** `/status`, `/status *`, `/ports`, `/list`, `/help`.  
- **External Alerts API:** Secure custom alerts with API keys.  
- **Swagger UI:** Interactive API docs.

For further details or customizations, refer to the source code or contact the maintainer.
