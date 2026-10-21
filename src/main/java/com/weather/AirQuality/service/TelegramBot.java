package com.weather.AirQuality.service;

import com.weather.AirQuality.config.BotConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final CallBackQueryHandler callBackQueryHandler;
    private final TextMessageHandler textMessageHandler;
    private final BotMessenger botMessenger;

    public TelegramBot(BotConfig botConfig,
                       CallBackQueryHandler callBackQueryHandler,
                       TextMessageHandler textMessageHandler,
                       BotMessenger botMessenger) {
        this.botConfig = botConfig;
        this.callBackQueryHandler = callBackQueryHandler;
        this.textMessageHandler = textMessageHandler;
        this.botMessenger = botMessenger;
    }

    @PostConstruct
    public void registerCommands() {
        botMessenger.bind(this);
        List<BotCommand> commands = List.of(
                new BotCommand("/start", "Запустить бота"),
                new BotCommand("/help", "Помощь")
        );
        try {
            execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error with the list of commands", e);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            callBackQueryHandler.handleCallbackQuery(update);
            return;
        }
        textMessageHandler.handleTextMessage(update);
    }
}
