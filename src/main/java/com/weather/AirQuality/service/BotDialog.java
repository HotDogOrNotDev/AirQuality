package com.weather.AirQuality.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BotDialog {

    static final String AIR_QUALITY_CALLBACK = "Air quality check";

    private final BotMessenger messenger;

    public BotDialog(BotMessenger messenger) {
        this.messenger = messenger;
    }

    public void start(long chatId, String name) {
        log.info("Replied to {}", name);
        messenger.send(chatId, "Привет, " + name + "! Рад знакомству.");
        menu(chatId);
    }

    public void help(long chatId) {
        messenger.send(chatId, """
                Список команд:
                /start — запустить бота
                /help — показать эту справку""");
    }

    public void askCity(long chatId) {
        messenger.send(chatId, "Введите город на английском, чтобы проверить качество воздуха:");
    }

    public void checkingCity(long chatId, String city) {
        messenger.send(chatId, "Проверяю качество воздуха в городе " + city + "...");
    }

    public void unknownCommand(long chatId) {
        messenger.send(chatId, "Неизвестная команда. Попробуйте ещё раз.");
    }

    public void callbackError(long chatId) {
        messenger.send(chatId, "Не удалось обработать запрос. Попробуйте позже.");
    }

    public void menu(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите, что нужно:");

        InlineKeyboardButton button = new InlineKeyboardButton("Проверить качество воздуха");
        button.setCallbackData(AIR_QUALITY_CALLBACK);

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);

        messenger.send(message);
        log.info("Sent a menu to the chat: {}", chatId);
    }
}
