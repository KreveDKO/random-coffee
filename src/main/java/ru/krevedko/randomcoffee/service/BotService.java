package ru.krevedko.randomcoffee.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.krevedko.randomcoffee.constant.State;
import ru.krevedko.randomcoffee.repository.UserRepository;
import ru.krevedko.randomcoffee.config.BotConfig;
import ru.krevedko.randomcoffee.config.Buttons;
import ru.krevedko.randomcoffee.config.Phrases;

import ru.krevedko.randomcoffee.constant.Commands;
import ru.krevedko.randomcoffee.exception.NullNicknameException;
import ru.krevedko.randomcoffee.model.User;
import ru.krevedko.randomcoffee.repository.PairRepository;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@EnableScheduling

public class BotService extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final Buttons buttons;
    private final UserRepository userRepository;
    private final Phrases phrases;
    private final PairRepository pairRepository;
    private final UserService userService;

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
            messageHandler(update);

        }
        if (update.hasCallbackQuery()) {
            buttonHandler(update);
        }
    }

    private void messageHandler(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        long chatId = message.getChatId();
        User user;
        try {
            user = userService.getOrCreateUser(message);
        } catch (NullNicknameException e) {
            sendMessage(chatId, phrases.getNoneNickname());
            return;
        }
        if (user.isBanned()) {
            sendMessage(chatId, phrases.getUserBanned().replace("{admins}", botConfig.getAdminsString(userRepository.getAdminsNickname())));
            return;
        }

        if (Commands.START.equals(text)) {
            sendStartMessage(chatId);
            return;
        } else if (Commands.TIME.equals(text)) {
            DateFormat df = new SimpleDateFormat("HH:mm");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, 0);
            String time = df.format(cal.getTime());
            sendMessage(chatId, "Московское время " + time);

            return;
        } else if (Commands.SHOW_INFO.equals(text)) {
            sendMessage(chatId, user.getCurrentUserText(phrases), buttons.getForwardToHelpButtons());
            return;
        } else if (Commands.HELP.equals(text)) {
            sendMessage(chatId, phrases.getHelp(), buttons.getHelpButtons());
            userService.updateCommand(user.getId(), Commands.HELP);
            return;
        } else if (Commands.ABOUT.equals(text)) {
            sendMessage(chatId, phrases.getAbout().replace("{admins}", botConfig.getAdminsString(userRepository.getAdminsNickname())), buttons.getForwardToHelpButtons());
            return;
        } else if (Commands.EDIT.equals(text)) {
            sendMessage(chatId, phrases.getFirstQuestion(), buttons.getEditButtons());
            userService.updateCommand(user.getId(), Commands.EDIT);
            return;
        } else if (Commands.STOP.equals(text)) {
            stopMessage(chatId);
            return;
        } else if (Commands.STATUS.equals(text)) {
            sendMessage(chatId, userService.statusMessage(chatId), buttons.getForwardToHelpButtons());
            return;
        } else if (Commands.CANCEL.equals(text)) {
            cancelHandler(chatId);
            return;
        }
        if (user.getEditState() != null)
            switch (user.getEditState()) {
                case State.CHANGE_NAME:
                    user.setName(text);
                    userRepository.save(user);
                    if (Commands.START.equals(user.getCurrentCommand())) {
                        user.setEditState(State.CHANGE_SQUAD);
                        sendMessage(chatId, phrases.getFirstEditSquad());
                    } else {
                        user.setEditState(null);
                        user.setCurrentCommand(null);
                        sendMessage(chatId, phrases.getChangeSuccess(), buttons.getEditButtons());
                    }
                    userRepository.save(user);
                    return;
                case State.CHANGE_ABOUT:
                    user.setAbout(text);
                    if (Commands.START.equals(user.getCurrentCommand())) {
                        user.setCurrentCommand(null);
                        user.setEditState(null);
                        sendMessage(chatId, user.getCurrentUserText(phrases));
                    } else {
                        user.setEditState(null);
                        user.setCurrentCommand(null);
                        sendMessage(chatId, phrases.getChangeSuccess(), buttons.getEditButtons());

                    }


                    userRepository.save(user);
                    return;
                case State.CHANGE_SQUAD:
                    user.setSquad(text);
                    if (user.getCurrentCommand().equals(Commands.START)) {
                        user.setEditState(State.CHANGE_ABOUT);
                        user.setActive(true);
                        sendMessage(chatId, phrases.getFirstEditAbout());
                    } else {
                        sendMessage(chatId, phrases.getChangeSuccess(), buttons.getEditButtons());
                        user.setEditState(null);
                        user.setCurrentCommand(null);

                    }
                    userRepository.save(user);
                    return;
            }
        //admin commads
        if (user.isAdmin()) {
            String[] args = text.split(" ");
            if (text.startsWith(Commands.BAN)) {
                String banUser = args[1].replace("@", "");
                if (userService.setBanUser(banUser, true))
                    sendMessage(chatId, phrases.getBanSuccess());
                else
                    sendMessage(chatId, phrases.getEditError());
                return;

            } else if (text.startsWith(Commands.UNBAN)) {
                String banUser = args[1].replace("@", "");
                if (userService.setBanUser(banUser, false))
                    sendMessage(chatId, phrases.getUnbanSuccess());
                else
                    sendMessage(chatId, phrases.getEditError());
                return;
            } else if (Commands.FORCE_PAIR.equals(text)) {
//                foundPair(true);
//                sendMessage(chatId, "Уведомления отправлены");
                return;
            } else if (Commands.ADMIN_PANEL.equals(text)) {
                sendMessage(chatId, phrases.getAdminPanel(), buttons.getAdminButtons());
                return;
            } else if (Commands.ADMIN_SHOW_INFO.equals(text)) {
                user.setCurrentCommand(Commands.ADMIN_SHOW_INFO);
                userRepository.save(user);
                sendMessage(chatId, phrases.getAdminEnterNickname());
                return;
            }
            if (user.getCurrentCommand() != null) {
                if (user.getCurrentCommand().equals(Commands.ADMIN_SHOW_INFO)) {
                    user.setCurrentCommand(null);
                    userRepository.save(user);
                    Optional<User> infoUser = userRepository.getUserByNick(text.replace("@", ""));
                    if (infoUser.isEmpty()) {
                        sendMessage(chatId, phrases.getAdminUserNotFound());
                        return;
                    }
                    sendMessage(chatId, infoUser.get().getCurrentUserText(phrases, true), buttons.getAdminUserInfo(infoUser.get()));
                    return;
                }
            }

        }
        sendMessage(chatId, phrases.getError());
    }

    private void buttonHandler(Update update) {
        log.info("{}", update.getCallbackQuery());
        CallbackQuery query = update.getCallbackQuery();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user = userRepository.findById(chatId).get();

        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
        String[] data = query.getData().split(";");
        String command = data[0];
        String args = null;
        if (data.length > 1)
            args = data[1];
        if (buttons.getHideButtons().getId().equals(command)) {
            updateMessage(chatId, messageId, update.getCallbackQuery().getMessage().getText());
            return;
        }
        if (buttons.getBack().getId().equals(command)) {
            if (buttons.getHelp().getId().equals(args)) {
                updateMessage(chatId, messageId, phrases.getHelp(), buttons.getHelpButtons());
                user.setEditState(null);
                user.setCurrentCommand(null);
                userRepository.save(user);
            }

        }
        if (buttons.getStart().getId().equals(command)) {
            user.setEditState(State.CHANGE_NAME);
            userRepository.save(user);
            updateMessage(chatId, messageId, phrases.getSucessStart());
            return;
        }
        if (buttons.getChangeName().getId().equals(command)) {
            user.setEditState(State.CHANGE_NAME);
            userRepository.save(user);
            updateMessage(chatId, messageId, phrases.getChangeNameText());
            return;
        }
        if (buttons.getChangeAbout().getId().equals(command)) {
            user.setEditState(State.CHANGE_ABOUT);
            userRepository.save(user);
            updateMessage(chatId, messageId, phrases.getChangeAboutText());
            return;
        }
        if (buttons.getChangeSquad().getId().equals(command)) {
            user.setEditState(State.CHANGE_SQUAD);
            userRepository.save(user);
            updateMessage(chatId, messageId, phrases.getChangeSquadText());
            return;
        }
        if (buttons.getChangeNickname().getId().equals(command)) {
            user.setNickname(query.getMessage().getChat().getUserName());
            userRepository.save(user);
            updateMessage(chatId, messageId, phrases.getChangeNicknameText(), buttons.getEditButtons());
            return;
        }
        if (buttons.getStop().getId().equals(command)) {
            deleteMessage(chatId, messageId);
            stopMessage(chatId);
            return;
        }
        if (buttons.getCancel().getId().equals(command)) {
            cancelHandler(chatId, messageId);
            return;
        } else if (command.startsWith("FEEDBACK_")) {
            updateMessage(chatId, messageId, phrases.getFeedBackSended());
            userService.saveFeedback(command, args);
        } else if (buttons.getAbout().getId().equals(command)) {
            updateMessage(chatId, messageId, phrases.getAbout().replace("{admins}", botConfig.getAdminsString(userRepository.getAdminsNickname())), buttons.getForwardToHelpButtons());
        } else if (buttons.getShowInfo().getId().equals(command)) {
            updateMessage(chatId, messageId, user.getCurrentUserText(phrases), buttons.getForwardToHelpButtons());
        } else if (buttons.getEdit().getId().equals(command)) {
            updateMessage(chatId, messageId, phrases.getFirstQuestion(), buttons.getEditButtons());
        }
        if (buttons.getStatus().getId().equals(command)) {
            updateMessage(chatId, messageId, userService.statusMessage(chatId), buttons.getForwardToHelpButtons());
        }

        if (user.isAdmin()) {
            if (buttons.getAdminShowUsers().getId().equals(command)) {

            } else if (buttons.getAdminBanUser().getId().equals(command)) {
                User bannedUser = userRepository.findById(Long.parseLong(args)).get();
                bannedUser.setBanned(true);
                userRepository.save(bannedUser);
                updateMessage(chatId, messageId, phrases.getBanSuccess());
                return;

            } else if (buttons.getAdminUnbanUser().getId().equals(command)) {
                User bannedUser = userRepository.findById(Long.parseLong(args)).get();
                bannedUser.setBanned(false);
                userRepository.save(bannedUser);
                updateMessage(chatId, messageId, phrases.getUnbanSuccess());
                return;

            } else if (buttons.getAdminShowInfo().getId().equals(command)) {
                if (args == null) {
                    user.setCurrentCommand(Commands.ADMIN_SHOW_INFO);
                    userRepository.save(user);
                    updateMessage(chatId, messageId, phrases.getAdminEnterNickname());

                }
            }

        }
    }

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
        }
    }

    private void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage message = new DeleteMessage();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
        }
    }

    private void updateMessage(Long chatId, Integer messageId, String text) {
        updateMessage(chatId, messageId, text, null);
    }

    private void updateMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(text);
        if (keyboardMarkup != null) {
            message.setReplyMarkup(keyboardMarkup);
        }
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
        }
    }

    private void cancelHandler(Long chatId) {
        cancelHandler(chatId, null);
    }

    private void cancelHandler(Long chatId, Integer messageId) {
        userRepository.findById(chatId).ifPresent(u -> {
            if (u.getCurrentCommand() == null || u.getCurrentCommand().isEmpty()) {
                deleteMessage(chatId, messageId);
                return;
            }
            if (messageId != null) {
                updateMessage(chatId, messageId, phrases.getCancelTemplate().replace("{command}", u.getCurrentCommand()));
            } else {
                sendMessage(chatId, phrases.getCancelTemplate().replace("{command}", u.getCurrentCommand()));
            }
            u.setCurrentCommand(null);
            userRepository.save(u);
        });
    }

    private void stopMessage(Long chatId) {
        User user = userRepository.findById(chatId).get();
        user.setActive(false);
        userRepository.save(user);
        sendMessage(chatId, phrases.getStopText());
    }

    private void sendStartMessage(Long chatId) {
        Optional<User> user = userRepository.findById(chatId);
        user.ifPresent(u -> {
            userRepository.save(u);
            sendMessage(chatId, phrases.getStart());
            sendMessage(chatId, phrases.getFirstQuestion(), buttons.getStartButtons());
            u.setCurrentCommand(Commands.START);
            userRepository.save(u);

        });
    }

    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    private void sendMessage(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (keyboard != null)
            message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("{}", e);
        }
    }

    public void sendFeedBack(Long chatId, Long pairId, String nickname) {
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        firstLine.add(buttons.getFeedBackSuccess().toTelegramButton(pairId));
        rowsInLine.add(firstLine);
        List<InlineKeyboardButton> secondLine = new ArrayList<>();
        secondLine.add(buttons.getFeedBackFailed().toTelegramButton(pairId));
        rowsInLine.add(secondLine);
        List<InlineKeyboardButton> thirdLine = new ArrayList<>();
        buttons.getFeedBackNotSure().toTelegramButton(pairId);
        thirdLine.add(buttons.getFeedBackNotSure().toTelegramButton());
        rowsInLine.add(thirdLine);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(phrases.getFeedBack().replace("{nickname}", nickname));
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        markupInLine.setKeyboard(rowsInLine);
        message.setReplyMarkup(markupInLine);

        try {
            execute(message);

        } catch (TelegramApiException e) {
            log.error("{}", e);
        }
    }
}
