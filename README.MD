# Monitoring Server Documentation

This document describes the functionality and architecture of the monitoring server, which is responsible for receiving data from remote agents, storing and processing metrics, sending alerts via Telegram, and performing additional health checks on registered agents.

---

## Architecture Overview

The server is implemented as a Spring Boot application and uses MongoDB as its data store. It provides the following key functionalities:

### 1. Agent Registration

Agents register themselves by sending their information (hostname, IP, list of open ports, timestamp, and the port they are listening on) to the endpoint `/api/agent/register`.

- **Data Storage:** The information is stored in the `agents` collection. If an agent with the same IP and hostname already exists, the record is updated.

### 2. Metrics Reception

Agents send system metrics (hostname, IP, timestamp, CPU usage, disk usage, and RAM usage) to the endpoint `/api/metrics`.

- **Data Storage:** Metrics are stored in the `metrics` collection within the `telemetrydb` database.
- **Alert Checking:** After saving, the server checks if any metric exceeds a critical threshold (CPU ≥ 95%, Disk ≥ 85%, RAM ≥ 80%). If a threshold is breached, an alert is sent via Telegram and recorded in the `alerts` collection. To avoid duplicate notifications, for each IP–metricType combination an alert is sent at most once every 24 hours.

### 3. Scheduled Port Status Check

A scheduled job (`PortStatusJob`) runs periodically (every 5 minutes) to verify that the ports reported by agents are still active.

- **Port Verification:** For each agent, the job iterates over the list of open ports and attempts a TCP connection to check if the port is up.
- **Alerting for Port Down:** If a port is not reachable, the job checks the `alerts` collection to determine if an alert for that IP, metric type "PORT", and port number has been sent in the last 24 hours. If not, an alert is sent via Telegram and recorded.
- **Lock Mechanism:**  
  To prevent concurrent or redundant executions, a lock is implemented using a dedicated collection (`jobLocks`).  
  - A static final UUID (`JOB_UUID`) is generated when the server instance starts.  
  - At the beginning of the job, the job checks for the presence of a lock with the `JOB_UUID`. If the lock exists, the job skips execution.  
  - If not, it creates the lock, runs the port verification, and at the end of the job, deletes the lock.  
  This ensures the job runs only once per cycle per server instance.

---

## Components and Endpoints

### 1. Agent Registration API

- **Endpoint:** `/api/agent/register`
- **Purpose:** Register a new agent or update an existing agent's details.
- **Data Model:**

```java
public class AgentInfo {
    private String hostname;
    private String ip;
    private List<Integer> openPorts;
    private long timestamp;
    private int agentPort;
}
```

- **Flow:**  
  The controller receives a JSON payload, processes it via a service that checks if an agent with the same IP and hostname exists. If found, it updates the record; otherwise, it creates a new record in the `agents` collection.

### 2. Metrics API

- **Endpoint:** `/api/metrics`
- **Purpose:** Receive system metrics from agents.
- **Data Model:**

```java
public class Metrics {
    private String hostname;
    private String ip;
    private long timestamp;
    private double cpuUsage;
    private double diskUsage;
    private double ramUsage;
}
```

- **Flow:**  
  The controller receives the JSON metrics payload, saves it into MongoDB, and invokes the alert service to check for critical thresholds and send alerts via Telegram if necessary.

### 3. Alerting System

- **Alerts Storage:** Alerts are stored in the `alerts` collection.
- **Data Model:**

```java
public class Alert {
    private String hostname;
    private String ip;
    private String metricType; // e.g., "CPU", "Disk", "RAM", "PORT"
    private Integer port;      // Only for PORT alerts
    private long timestamp;
}
```

- **Logic:**  
  For each metric, the server checks if an alert has been sent in the last 24 hours for the same IP–metricType pair. If not, it sends a Telegram alert and saves the alert record.

### 4. Scheduled Port Status Job

- **Class:** `PortStatusJob`
- **Schedule:** Runs every 5 minutes.
- **Functionality:**
  - Retrieves all registered agents from the `agents` collection.
  - For each agent, iterates through the list of open ports.
  - Uses a TCP connection attempt to verify if each port is open.
  - For any port that is down, it checks the `alerts` collection for a recent alert (last 24 hours) for that IP and port with metric type “PORT”.
  - If no recent alert is found, sends a Telegram alert and records the alert.

- **Locking Mechanism:**  
  The job uses a static final UUID (`JOB_UUID`) as its lock identifier. At the start, it checks if a lock with this UUID exists in the `jobLocks` collection. If it does, the job is skipped. Otherwise, the lock is created, the job runs, and finally the lock is removed.

### 5. Job Lock Data Model

- **Data Model:**

```java
public class JobLock {
    private String id;      // Set to JOB_UUID
    private String jobName; // e.g., "PortStatusJob"
    private String lockId;  // Also JOB_UUID
    private long timestamp;
}
```

- **Repository:**  
  The repository supports finding a lock by its ID and deleting it after job execution.

---

## Configuration

### MongoDB

- **Database:** `telemetrydb`
- **Collections:**
  - `metrics` for system metrics.
  - `agents` for registered agents.
  - `alerts` for alert records.
  - `jobLocks` for job execution locks.

### Telegram

Configure the following properties in `application.properties` or `application.yml`:

```
telegram.bot.token=your_bot_token_here
telegram.chat.id=your_chat_id_here
```

### Scheduling

Ensure that scheduling is enabled in your main application class:

```java
@SpringBootApplication
@EnableScheduling
public class ServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }
}
```

---

## Conclusion

The monitoring server is designed to:

- **Register Agents:**  
  Accept registration from agents and update their information as needed.

- **Store Metrics:**  
  Save incoming metrics and trigger alerts for critical conditions (CPU, Disk, RAM) with a 24-hour alert window to avoid duplicates.

- **Scheduled Port Verification:**  
  Periodically verify the health of reported open ports for each agent. If a port is down, an alert is sent via Telegram, and duplicate alerts for the same port are suppressed for 24 hours using the alert history.

- **Locking Mechanism for Jobs:**  
  Use a persistent job lock (stored in `jobLocks`) with a static final UUID to prevent concurrent or redundant job executions, ensuring that the scheduled job runs only once per cycle and cleans up its lock after execution.

This design ensures robustness, scalability, and timely alerting, making the monitoring system reliable even as the number of monitored agents increases.

Feel free to reach out if you need further clarifications or additional features.
