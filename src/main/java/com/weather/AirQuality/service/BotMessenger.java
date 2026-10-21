package com.weather.AirQuality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class BotMessenger {

    private AbsSender sender;

    public void bind(AbsSender sender) {
        this.sender = sender;
    }

    public void send(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        send(message);
    }

    public void sendMarkdown(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode(ParseMode.MARKDOWN);
        send(message);
    }

    public void send(SendMessage message) {
        if (sender == null) {
            throw new IllegalStateException("Telegram bot is not bound");
        }
        try {
            sender.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred while sending message: {}", e.getMessage(), e);
        }
    }
}
