package org.cheetah.monitoring.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

/**
 * Registers available bot commands with Telegram via setMyCommands API.
 */
@Component
public class TelegramCommandConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.commands.status.description}")
    private String statusDesc;

    @Value("${telegram.commands.ports.description}")
    private String portsDesc;

    @Value("${telegram.commands.help.description}")
    private String helpDesc;
    
    @Value("${telegram.commands.list.description}")
    private String listDesc;
    
    private final RestTemplate rest = new RestTemplate();

    /**
     * Invoked after bean creation to register bot commands with descriptions.
     */
    @PostConstruct
    public void registerCommands() {
        String url = "https://api.telegram.org/bot" + botToken + "/setMyCommands";
        String body = String.format(
            "{\"commands\":[{\"command\":\"status\",\"description\":\"%s\"},"
          + "{\"command\":\"ports\",\"description\":\"%s\"},"
          + "{\"command\":\"help\",\"description\":\"%s\"}]}"
          + "{\"command\":\"list\",  \"description\":\"%s\"},",
            statusDesc, portsDesc, helpDesc,listDesc
        );
        Object obj = rest.postForObject(url, body, String.class);
        System.out.println(obj);
    }
}