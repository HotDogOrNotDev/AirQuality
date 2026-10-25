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
    static final String STATIONS_CALLBACK_PREFIX = "stations:";

    private final BotMessenger messenger;

    public BotDialog(BotMessenger messenger) {
        this.messenger = messenger;
    }

    public static final String DESCRIPTION = """
            Показывает качество воздуха по данным WAQI: PM2.5, температуру, влажность и короткую рекомендацию.

            Город пишите на английском: moscow.
            Станцию можно уточнить через запятую: Moscow, Spirid.""";

    public static final String SHORT_DESCRIPTION =
            "Качество воздуха по городу: PM2.5, температура, влажность и рекомендация.";

    public void start(long chatId, String name) {
        log.info("Replied to {}", name);
        messenger.send(chatId, "Привет, " + name + "!");
        menu(chatId);
    }

    public void help(long chatId) {
        messenger.send(chatId, """
                Бот запрашивает станцию WAQI и присылает PM2.5, температуру, влажность и рекомендацию.

                Как писать запрос:
                moscow — основная станция города
                Moscow, Spirid — станция, в имени которой есть это слово
                Moscow,Spirid — то же самое, пробел после запятой не обязателен

                Регистр не важен. Город нужен на английском: так его понимает WAQI.
                Если подходят несколько станций, бот пришлёт список, чтобы название можно было уточнить.
                Если станция не найдена, можно запросить все станции этого города.

                Команды:
                /start — начать
                /help — эти правила""");
    }

    public void askCity(long chatId) {
        messenger.send(chatId, """
                Введите город на английском.
                Чтобы выбрать станцию, добавьте часть её названия через запятую: Moscow, Spirid""");
    }

    public void stationNotFound(long chatId, String city) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Станция не найдена. Можно посмотреть все станции этого города.");

        InlineKeyboardButton button = new InlineKeyboardButton("Все станции");
        button.setCallbackData(STATIONS_CALLBACK_PREFIX + city);

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        message.setReplyMarkup(markup);
        messenger.send(message);
    }

    public void checkingCity(long chatId, String city) {
        messenger.send(chatId, "Проверяю качество воздуха: " + city + "...");
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
        message.setText("""
                Напишите город на английском — пришлю PM2.5, температуру, влажность и рекомендацию.

                moscow — основная станция города
                Moscow, Spirid — станция, в имени которой есть это слово""");

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
