package org.cheetah.monitoring.controllers;

import org.cheetah.monitoring.services.BotTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bottest")
public class BotTestController {

    private final BotTestService botTestService;

    public BotTestController(BotTestService botTestService) {
        this.botTestService = botTestService;
    }

    /**
     * Endpoint to send a test message through the Telegram bot.
     * Example: GET /api/bottest?message=HelloTest
     *
     * @param message The test message (optional, default provided).
     * @return a ResponseEntity with status OK.
     */
    @GetMapping
    public ResponseEntity<String> testBot(@RequestParam(value = "message", defaultValue = "Test message: Bot is working!") String message) {
        botTestService.testBot(message);
        return ResponseEntity.ok("Test message sent: " + message);
    }
}