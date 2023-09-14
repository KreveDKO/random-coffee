package ru.krevedko.randomcoffee.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.krevedko.randomcoffee.config.BotConfig;
import ru.krevedko.randomcoffee.constant.CallbackStatus;
import ru.krevedko.randomcoffee.model.Button;
import ru.krevedko.randomcoffee.model.ServiceCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BotService extends TelegramLongPollingBot implements IBotService {

    private final BotConfig botConfig;
    private final UserService userService;
    private final CoffeeService coffeeService;

    @Override
    public String getBotUsername() {

        return botConfig.getName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onRegister() {
        log.info("Bot start with {} version", botConfig.getConfigVersion());
        super.onRegister();
    }

    @Override
    public void onUpdateReceived(Update update) {

        log.info("{}", update);
        if (update.hasMessage()) {
            if (!update.getMessage().getChat().getType().equals("private")) return;
            if (!update.getMessage().hasText()) return;
            log.info("{}", update.getMessage().getChat().getType());
            Message message = update.getMessage();
            String text = message.getText();
            long chatId = message.getChatId();
            String nickname = update.getMessage().getFrom().getUserName();
            ServiceCallback callback = coffeeService.handleMessage(chatId, text, nickname);
            if (callback.getStatus().equals(CallbackStatus.SEND_MESSAGE)) {
                sendMessage(chatId, callback.getMessage(), callback.getButtons());
            }

        }
        if (update.hasCallbackQuery()) {
            CallbackQuery query = update.getCallbackQuery();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            ServiceCallback callback = coffeeService.buttonHandler(chatId, query.getData(), query.getFrom().getUserName());
            switch (callback.getStatus()) {
                case SEND_MESSAGE:
                    sendMessage(chatId, callback.getMessage());
                    break;
                case UPDATE_MESSAGE:
                    if (callback.getMessage() != null) {
                        updateMessage(chatId, messageId, callback.getMessage(), callback.getButtons());
                    } else {
                        updateMessage(chatId, messageId, query.getMessage().getText(), callback.getButtons());
                    }
                    break;
                case DELETE_MESSAGE:
                    deleteMessage(chatId, messageId);
                    break;
            }

        }
    }

    @Override
    public void sendPhoto(Long chatId, File file, String caption) {

        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setCaption(caption);
        InputFile inputFile = new InputFile(file);
        message.setPhoto(inputFile);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
            if (e.getMessage().contains("bot was blocked by the user")) ;
            userService.setActivateUser(chatId, false);
        }
    }

    public void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage message = new DeleteMessage();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
        }
    }


    public void updateMessage(Long chatId, Integer messageId, String text, List<List<Button>> keyboardMarkup) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(text);
        if (keyboardMarkup != null) {
            message.setReplyMarkup(toTelegramButtons(keyboardMarkup));
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
        }
    }

    @Override
    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    @Override
    public void sendMessage(Long chatId, String text, List<List<Button>> keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null)
            message.setReplyMarkup(toTelegramButtons(keyboard));
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
            if (e.getMessage().contains("bot was blocked by the user")) ;
            userService.setActivateUser(chatId, false);

        }
    }

    private InlineKeyboardMarkup toTelegramButtons(List<List<Button>> buttons) {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (List<Button> buttonList : buttons) {
            List<InlineKeyboardButton> row = buttonList.stream().map(b -> b.toTelegramButton()).collect(Collectors.toList());
            rowsInLine.add(row);
        }
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }
}
