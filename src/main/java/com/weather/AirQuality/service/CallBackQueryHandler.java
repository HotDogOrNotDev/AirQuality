package com.weather.AirQuality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class CallBackQueryHandler {

    private final BotDialog botDialog;

    public CallBackQueryHandler(BotDialog botDialog) {
        this.botDialog = botDialog;
    }

    public void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            if (BotDialog.AIR_QUALITY_CALLBACK.equals(callbackData)) {
                botDialog.askCity(chatId);
                return;
            }
            log.warn("Unknown callback data received: {}", callbackData);
            botDialog.unknownCommand(chatId);
        } catch (Exception e) {
            log.error("Error while handling callback query: {}", callbackData, e);
            botDialog.callbackError(chatId);
        }
    }
}
