package org.cheetah.monitoring.services;

import org.cheetah.monitoring.model.AgentInfo;
import org.cheetah.monitoring.model.Metrics;
import org.cheetah.monitoring.repositories.AgentInfoRepository;
import org.cheetah.monitoring.repositories.MetricsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles incoming Telegram updates and responds to commands.
 */
@Service
public class TelegramBotService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate rest = new RestTemplate();
    private final AgentInfoRepository agentRepo;
    private final MetricsRepository metricsRepo;

    private final ObjectMapper mapper = new ObjectMapper();

    public TelegramBotService(AgentInfoRepository agentRepo,
                              MetricsRepository metricsRepo) {
        this.agentRepo = agentRepo;
        this.metricsRepo = metricsRepo;
    }

    /**
     * Main entry point for processing each Telegram update.
     */
    public void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText().trim();
            String[] parts = text.split("\\s+", 2);
            String cmd = parts[0].toLowerCase();
            String arg = parts.length > 1 ? parts[1] : "";

            String response;
            switch (cmd) {
                case "/status":
                    response = handleStatus(arg);
                    break;
                case "/ports":
                    response = handlePorts(arg);
                    break;
                case "/help":
                    response = getHelp();
                    break;
                default:
                    response = "Unknown command. Use /help to see available commands.";
            }
            sendMessage(chatId, response);
        }
    }

    /**
     * Handles the /status command, returning the latest metrics for a given hostname.
     */
    private String handleStatus(String hostname) {
        if (hostname.isBlank()) {
            return "Usage: /status <hostname>";
        }
        Metrics m = metricsRepo.findTopByHostnameOrderByTimestampDesc(hostname);
        if (m == null) return "No metrics found for host: " + hostname;
        return String.format(
          "Status for %s:\nCPU: %.2f%%\nDisk: %.2f%%\nRAM: %.2f%%\nTimestamp: %d",
          hostname, m.getCpuUsage(), m.getDiskUsage(), m.getRamUsage(), m.getTimestamp());
    }

    /**
     * Handles the /ports command, listing open ports for a given hostname.
     */
    private String handlePorts(String hostname) {
        if (hostname.isBlank()) {
            return "Usage: /ports <hostname>";
        }
        AgentInfo agent = agentRepo.findByHostname(hostname);
        if (agent == null) return "No agent registered with hostname: " + hostname;
        return "Open ports for " + hostname + ": " + agent.getOpenPorts();
    }

    /**
     * Returns a help message listing available commands.
     */
    private String getHelp() {
        return String.join("\n",
            "Available commands:",
            "/status <hostname> - get latest metrics",
            "/ports <hostname>  - list open ports",
            "/help              - this help message"
        );
    }

    /**
     * Sends a text message back to the Telegram chat.
     */
    private void sendMessage(String chatId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", chatId);
        params.add("text", text);
        rest.postForObject(url, params, String.class);
    }
}