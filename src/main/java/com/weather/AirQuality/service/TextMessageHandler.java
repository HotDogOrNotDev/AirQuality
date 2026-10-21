package com.weather.AirQuality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class TextMessageHandler {

    private final BotDialog botDialog;
    private final AirQualityService airQualityService;

    public TextMessageHandler(BotDialog botDialog, AirQualityService airQualityService) {
        this.botDialog = botDialog;
        this.airQualityService = airQualityService;
    }

    public void handleTextMessage(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        switch (messageText) {
            case "/start" -> botDialog.start(chatId, update.getMessage().getChat().getFirstName());
            case "/help" -> botDialog.help(chatId);
            default -> {
                log.info("Received message from chatId {}", chatId);
                botDialog.checkingCity(chatId, messageText);
                airQualityService.sendAirQualityNow(chatId, messageText);
            }
        }
    }
}
