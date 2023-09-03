package ru.krevedko.randomcoffee.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.krevedko.randomcoffee.model.Button;
import ru.krevedko.randomcoffee.model.User;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "buttons")
public class Buttons {
    Button feedBackSuccess;
    Button feedBackFailed;
    Button feedBackNotSure;

    Button cancel;
    Button start;
    Button hideButtons;

    Button changeName;
    Button changeAbout;
    Button changeSquad;
    Button changeNickname;

    Button help;
    Button about;
    Button showInfo;
    Button edit;
    Button status;
    Button stop;
    Button back;

    Button adminShowUsers;
    Button adminShowMoreUsers;
    Button adminShowInfo;
    Button adminBanUser;
    Button adminUnbanUser;

    public InlineKeyboardMarkup getStartButtons() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        rowsInLine.add(getLineButton(start));
        rowsInLine.add(getLineButton(cancel));
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public InlineKeyboardMarkup getEditButtons() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        firstLine.add(getChangeName().toTelegramButton());
        rowsInLine.add(firstLine);
        firstLine.add(getChangeSquad().toTelegramButton());

        List<InlineKeyboardButton> secondLine = new ArrayList<>();
        secondLine.add(getChangeAbout().toTelegramButton());
        secondLine.add(getChangeNickname().toTelegramButton());
        rowsInLine.add(secondLine);

        List<InlineKeyboardButton> thirdLine = new ArrayList<>();
        thirdLine.add(getBack().toTelegramButton(help.getId()));
        rowsInLine.add(thirdLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public InlineKeyboardMarkup getHelpButtons() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
//        Buttons[] test = new ArrayList<Button>(about);
        rowsInLine.add(getLineButton(about));
        rowsInLine.add(getLineButton(showInfo));
        rowsInLine.add(getLineButton(edit));
        rowsInLine.add(getLineButton(status));
        rowsInLine.add(getLineButton(stop));
        rowsInLine.add(getLineButton(hideButtons));
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public InlineKeyboardMarkup getForwardToHelpButtons() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        firstLine.add(back.toTelegramButton(help.getId()));
        firstLine.add(hideButtons.toTelegramButton());
        rowsInLine.add(firstLine);
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public InlineKeyboardMarkup getAdminButtons() {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        rowsInLine.add(getLineButton(adminShowUsers));
        rowsInLine.add(getLineButton(adminShowInfo));
        rowsInLine.add(getLineButton(adminBanUser));
        rowsInLine.add(getLineButton(adminUnbanUser));
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public InlineKeyboardMarkup getAdminUserInfo(User user) {
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> buttonsLine = new ArrayList<>();
        buttonsLine.add(adminBanUser.toTelegramButton(user.getId()));
        buttonsLine.add(adminUnbanUser.toTelegramButton(user.getId()));
        buttonsLine.add(hideButtons.toTelegramButton());
        rowsInLine.add(buttonsLine);
        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    public List<InlineKeyboardButton> getLineButton(Button button) {
        List<InlineKeyboardButton> buttonsLine = new ArrayList<>();
        buttonsLine.add(button.toTelegramButton());
        return buttonsLine;
    }

    public List<InlineKeyboardButton> getLineButton(InlineKeyboardButton button) {
        List<InlineKeyboardButton> buttonsLine = new ArrayList<>();
        buttonsLine.add(button);
        return buttonsLine;
    }




}
