package ru.krevedko.randomcoffee.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;
import ru.krevedko.randomcoffee.config.Buttons;
import ru.krevedko.randomcoffee.config.Phrases;
import ru.krevedko.randomcoffee.constant.Commands;
import ru.krevedko.randomcoffee.constant.State;
import ru.krevedko.randomcoffee.exception.NullNicknameException;
import ru.krevedko.randomcoffee.model.ServiceCallback;
import ru.krevedko.randomcoffee.model.User;
import ru.krevedko.randomcoffee.repository.UserRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CoffeeService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final Phrases phrases;
    private final Buttons buttons;

    private String getAdminsString(List<String> admins) {
        return admins.stream().map(c -> "@" + c).collect(Collectors.joining(" "));
    }

    public ServiceCallback handleMessage(Long chatId, String text, String nickname) {
        User user;
        try {
            user = userService.getOrCreateUser(chatId, nickname);
        } catch (NullNicknameException e) {
            return ServiceCallback.sendMessageCallback(phrases.getNoneNickname());
        }
        if (user.isBanned()) {
            return ServiceCallback.sendMessageCallback(phrases.getUserBanned().replace("{admins}", getAdminsString(userRepository.getAdminsNickname())));
        }

        if (Commands.START.equals(text)) {
            Optional<User> userOpt = userRepository.findById(chatId);
            userOpt.ifPresent(u -> {
                userRepository.save(u);
                u.setCurrentCommand(Commands.START);
                userRepository.save(u);
            });
            return ServiceCallback.sendMessageCallback(phrases.getStart(), buttons.getStartButtons());

        } else if (Commands.TIME.equals(text)) {
            DateFormat df = new SimpleDateFormat("HH:mm");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, 0);
            String time = df.format(cal.getTime());
            return ServiceCallback.sendMessageCallback("Московское время " + time);

        } else if (Commands.SHOW_INFO.equals(text)) {
            return ServiceCallback.sendMessageCallback(user.getCurrentUserText(phrases), buttons.getForwardToHelpButtons());
        } else if (Commands.HELP.equals(text)) {
            userService.updateCommand(user.getId(), Commands.HELP);
            return ServiceCallback.sendMessageCallback(phrases.getHelp(), buttons.getHelpButtons());
        } else if (Commands.ABOUT.equals(text)) {
            return ServiceCallback.sendMessageCallback(phrases.getAbout().replace("{admins}", getAdminsString(userRepository.getAdminsNickname())), buttons.getForwardToHelpButtons());
        } else if (Commands.EDIT.equals(text)) {
            userService.updateCommand(user.getId(), Commands.EDIT);
            return ServiceCallback.sendMessageCallback(phrases.getEdit(), buttons.getEditButtons());
        } else if (Commands.STOP.equals(text)) {
            user.setActive(false);
            userRepository.save(user);
            return ServiceCallback.sendMessageCallback(phrases.getStopText());
        } else if (Commands.STATUS.equals(text)) {

            return ServiceCallback.sendMessageCallback(userService.statusMessage(chatId), buttons.getForwardToHelpButtons());
        } else if (Commands.CANCEL.equals(text) && user.getCurrentCommand() != null) {
            userRepository.findById(chatId).ifPresent(u -> {
                u.setCurrentCommand(null);
                u.setEditState(null);
                userRepository.save(u);
            });
            return ServiceCallback.sendMessageCallback(
                    phrases.getCancelTemplate()
                            .replace("{command}", user.getCurrentCommand())
            );
        }
        if (user.getEditState() != null) switch (user.getEditState()) {
            case State.CHANGE_NAME:
                user.setName(text);
                if (Commands.START.equals(user.getCurrentCommand())) {
                    user.setEditState(State.CHANGE_SQUAD);
                    userRepository.save(user);
                    return ServiceCallback.sendMessageCallback(phrases.getFirstEditSquad());
                } else {
                    user.setEditState(null);
                    user.setCurrentCommand(null);
                    userRepository.save(user);
                    return ServiceCallback.sendMessageCallback(phrases.getChangeSuccess(), buttons.getEditButtons());
                }
            case State.CHANGE_ABOUT:
                user.setAbout(text);
                if (Commands.START.equals(user.getCurrentCommand())) {
                    user.setCurrentCommand(null);
                    user.setEditState(null);
                    userRepository.save(user);
                    return ServiceCallback.sendMessageCallback(user.getCurrentUserText(phrases));
                } else {
                    user.setEditState(null);
                    user.setCurrentCommand(null);
                    userRepository.save(user);
                    return ServiceCallback.sendMessageCallback(phrases.getChangeSuccess(), buttons.getEditButtons());

                }
            case State.CHANGE_SQUAD:
                user.setSquad(text);
                if (user.getCurrentCommand().equals(Commands.START)) {
                    user.setEditState(State.CHANGE_ABOUT);
                    user.setActive(true);
                    userRepository.save(user);
                    return ServiceCallback.sendMessageCallback(phrases.getFirstEditAbout());
                } else {
                    user.setEditState(null);
                    user.setCurrentCommand(null);
                    userRepository.save(user);
                    return ServiceCallback.sendMessageCallback(phrases.getChangeSuccess(), buttons.getEditButtons());

                }
        }
        //admin commads
        if (user.isAdmin()) {
            String[] args = text.split(" ");
            if (text.startsWith(Commands.BAN)) {
                String banUser = args[1].replace("@", "");
                if (userService.setBanUser(banUser, true))
                    return ServiceCallback.sendMessageCallback(phrases.getBanSuccess());
                else return ServiceCallback.sendMessageCallback(phrases.getEditError());
            } else if (text.startsWith(Commands.UNBAN)) {
                String banUser = args[1].replace("@", "");
                if (userService.setBanUser(banUser, false))
                    return ServiceCallback.sendMessageCallback(phrases.getUnbanSuccess());
                else return ServiceCallback.sendMessageCallback(phrases.getEditError());
            } else if (Commands.ADMIN_PANEL.equals(text)) {
                return ServiceCallback.sendMessageCallback(phrases.getAdminPanel(), buttons.getAdminButtons());
            } else if (Commands.ADMIN_SHOW_INFO.equals(text)) {
                user.setCurrentCommand(Commands.ADMIN_SHOW_INFO);
                userRepository.save(user);
                return ServiceCallback.sendMessageCallback(phrases.getAdminEnterNickname());
            }
            if (user.getCurrentCommand() != null) {
                if (user.getCurrentCommand().equals(Commands.ADMIN_SHOW_INFO)) {
                    user.setCurrentCommand(null);
                    userRepository.save(user);
                    Optional<User> infoUser = userRepository.getUserByNick(text.replace("@", ""));
                    if (infoUser.isEmpty()) {
                        return ServiceCallback.sendMessageCallback(phrases.getAdminUserNotFound());
                    }
                    return ServiceCallback.sendMessageCallback(infoUser.get().getCurrentUserText(phrases, true), buttons.getAdminUserInfo(infoUser.get()));
                }
            }
        }
        return ServiceCallback.sendMessageCallback(phrases.getError());
    }

    public ServiceCallback buttonHandler(Long chatId, String dataQuery, String nickname) {
        User user = userRepository.findById(chatId).get();
        String[] data = dataQuery.split(";");
        String command = data[0];
        String args = null;
        if (data.length > 1) args = data[1];
        if (buttons.getHideButtons().getId().equals(command)) {

            return ServiceCallback.updateMessageCallback(null, null);
        }
        if (buttons.getBack().getId().equals(command)) {
            {
                user.setEditState(null);
                user.setCurrentCommand(null);
                userRepository.save(user);
                return ServiceCallback.updateMessageCallback(phrases.getHelp(), buttons.getHelpButtons());
            }

        }
        if (buttons.getStart().getId().equals(command)) {
            user.setEditState(State.CHANGE_NAME);
            userRepository.save(user);
            return ServiceCallback.updateMessageCallback(phrases.getSucessStart());
        }
        if (buttons.getChangeName().getId().equals(command)) {
            user.setEditState(State.CHANGE_NAME);
            userRepository.save(user);
            return ServiceCallback.updateMessageCallback(phrases.getChangeNameText());
        }
        if (buttons.getChangeAbout().getId().equals(command)) {
            user.setEditState(State.CHANGE_ABOUT);
            userRepository.save(user);
            return ServiceCallback.updateMessageCallback(phrases.getChangeAboutText());
        }
        if (buttons.getChangeSquad().getId().equals(command)) {
            user.setEditState(State.CHANGE_SQUAD);
            userRepository.save(user);
            return ServiceCallback.updateMessageCallback(phrases.getChangeSquadText());
        }
        if (buttons.getChangeNickname().getId().equals(command)) {
            user.setNickname(nickname);
            userRepository.save(user);
            return ServiceCallback.updateMessageCallback(phrases.getChangeNicknameText(), buttons.getEditButtons());
        }
        if (buttons.getStop().getId().equals(command)) {
            user.setActive(false);
            userRepository.save(user);
            return ServiceCallback.updateMessageCallback(phrases.getStopText());
        }
        if (command.startsWith("FEEDBACK_")) {
            userService.saveFeedback(command, args);
            return ServiceCallback.updateMessageCallback(phrases.getFeedBackSended());
        } else if (buttons.getAbout().getId().equals(command)) {
            return ServiceCallback.updateMessageCallback(phrases.getAbout().replace("{admins}", getAdminsString(userRepository.getAdminsNickname())), buttons.getForwardToHelpButtons());
        } else if (buttons.getShowInfo().getId().equals(command)) {
            return ServiceCallback.updateMessageCallback(user.getCurrentUserText(phrases), buttons.getForwardToHelpButtons());
        } else if (buttons.getEdit().getId().equals(command)) {
            return ServiceCallback.updateMessageCallback(phrases.getEdit(), buttons.getEditButtons());
        }
        if (buttons.getStatus().getId().equals(command)) {
            return ServiceCallback.updateMessageCallback(userService.statusMessage(chatId), buttons.getForwardToHelpButtons());
        }

        if (user.isAdmin()) {
            if (buttons.getAdminShowUsers().getId().equals(command)) {

            } else if (buttons.getAdminBanUser().getId().equals(command)) {
                User bannedUser = userRepository.findById(Long.parseLong(args)).get();
                bannedUser.setBanned(true);
                userRepository.save(bannedUser);
                return ServiceCallback.updateMessageCallback(phrases.getBanSuccess());


            } else if (buttons.getAdminUnbanUser().getId().equals(command)) {
                User bannedUser = userRepository.findById(Long.parseLong(args)).get();
                bannedUser.setBanned(false);
                userRepository.save(bannedUser);
                return ServiceCallback.updateMessageCallback(phrases.getUnbanSuccess());


            } else if (buttons.getAdminShowInfo().getId().equals(command)) {
                if (args == null) {
                    user.setCurrentCommand(Commands.ADMIN_SHOW_INFO);
                    userRepository.save(user);
                    return ServiceCallback.updateMessageCallback(phrases.getAdminEnterNickname());

                }
            }

        }
        return ServiceCallback.defaultCallback();
    }

}
