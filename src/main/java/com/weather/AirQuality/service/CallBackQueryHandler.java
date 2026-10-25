package com.weather.AirQuality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class CallBackQueryHandler {

    private final BotDialog botDialog;
    private final AirQualityService airQualityService;

    public CallBackQueryHandler(BotDialog botDialog, AirQualityService airQualityService) {
        this.botDialog = botDialog;
        this.airQualityService = airQualityService;
    }

    public void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            if (BotDialog.AIR_QUALITY_CALLBACK.equals(callbackData)) {
                botDialog.askCity(chatId);
                return;
            }
            if (callbackData.startsWith(BotDialog.STATIONS_CALLBACK_PREFIX)) {
                airQualityService.sendCityStations(chatId, callbackData.substring(BotDialog.STATIONS_CALLBACK_PREFIX.length()));
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
