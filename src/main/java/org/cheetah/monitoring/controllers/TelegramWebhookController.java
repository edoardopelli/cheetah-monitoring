package org.cheetah.monitoring.controllers;

import org.cheetah.monitoring.services.TelegramBotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Receives Telegram webhook updates and delegates to bot service.
 */
@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final TelegramBotService botService;

    public TelegramWebhookController(TelegramBotService botService) {
        this.botService = botService;
    }

    /**
     * Endpoint for Telegram to POST updates. Returns 200 OK immediately.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> onUpdateReceived(@RequestBody Update update) {
        botService.handleUpdate(update);
        return ResponseEntity.ok().build();
    }
}