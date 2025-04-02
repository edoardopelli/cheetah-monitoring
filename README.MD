# Monitoring Application Documentation

This application is designed to continuously monitor remote systems. It is composed of two main components:

- **Monitoring Server:**  
  A Spring Boot application that:
  - Receives metrics data (hostname, IP, timestamp, CPU usage, disk usage, and RAM usage) sent by remote agents.
  - Saves the metrics in a MongoDB database (database name: `telemetrydb`).
  - Checks if any metric exceeds a critical threshold and, if so, sends an alert via Telegram.
  - Records the alerts in an `alerts` collection to avoid duplicate notifications. For each `ip-metricType` pair, an alert is sent only if none has been sent in the last 24 hours.

- **Monitoring Agent:**  
  A lightweight agent written in Go that:
  - Collects system metrics using `gopsutil`.
  - Builds a JSON object with the collected data.
  - Sends the metrics to the server via an HTTP POST request.
  - Is configurable through environment variables for setting the server host, port, and data sending interval.

> **Note:** The agent repository is available at [github.com/edoardopelli/cheetah-monitoring-agent](https://github.com/edoardopelli/cheetah-monitoring-agent).

---

## How It Works

1. **Data Collection (Agent):**
   - The agent collects system metrics from the local machine (hostname, IP, timestamp, CPU, disk, and RAM usage).
   - Environment variables `MONITORING_SERVER_HOST`, `MONITORING_SERVER_PORT`, and `SEND_INTERVAL` define the server address and the interval (in seconds) between each data transmission.
   - The agent sends the metrics as JSON to the server endpoint `/api/metrics`.

2. **Data Reception and Storage (Server):**
   - The server exposes a REST endpoint to receive the metrics.
   - The received metrics are stored in the `metrics` collection of the `telemetrydb` database.

3. **Alert Checking and Sending (Server):**
   - After saving the data, the server checks if the metrics exceed the following critical thresholds:
     - **CPU:** 95%
     - **Disk:** 85%
     - **RAM:** 80%
   - For each critical metric, the server verifies whether an alert has already been sent for the same `ip-metricType` pair in the last 24 hours.  
     - If **no** alert exists, the server sends an alert via Telegram and records the alert in the `alerts` collection (including the hostname, IP, metric type, and the time of sending).
     - If an alert was already sent within 24 hours, a new alert is not sent until the 24-hour window has elapsed.

4. **Telegram Integration:**
   - Alerts are sent using the Telegram Bot API.
   - The properties `telegram.bot.token` and `telegram.chat.id` are configured in the `application.properties` file.
   - The alert message includes the system's IP, hostname, and the percentage of the metric that exceeded the threshold.

---

## Configuration

### Server (Spring Boot)
In your `application.properties` or `application.yml` file, include the following:

```properties
spring.data.mongodb.database=telemetrydb
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017

telegram.bot.token=your_bot_token_here
telegram.chat.id=your_chat_id_here

	•	telegram.bot.token: Your Telegram bot token (obtained from BotFather).
	•	telegram.chat.id: The ID of the chat where alerts should be sent.

Agent (Go)

The following environment variables configure the agent:
	•	MONITORING_SERVER_HOST: The host of the monitoring server (default: localhost).
	•	MONITORING_SERVER_PORT: The port of the monitoring server (default: 8080).
	•	SEND_INTERVAL: The interval (in seconds) between sending metrics (default: 60).

⸻

Technologies Used
	•	Server:
	•	Java 21
	•	Spring Boot
	•	Spring Data MongoDB
	•	Lombok
	•	MongoDB
	•	Telegram Bot API
	•	Agent:
	•	Go (developed using Visual Studio Code)
	•	gopsutil for metric collection
	•	HTTP client for data transmission

⸻

Conclusion

The monitoring application provides a robust and scalable system that:
	•	Continuously collects and stores metrics from remote systems.
	•	Sends intelligent alerts via Telegram, avoiding duplicate notifications within a 24-hour window for the same issue.
	•	Is easily configurable and extendable for future improvements.

This documentation outlines the functionality and configuration of the application, assisting with maintenance and future enhancements.

