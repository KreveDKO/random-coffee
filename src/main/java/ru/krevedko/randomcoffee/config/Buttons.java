package ru.krevedko.randomcoffee.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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

    private Button getCopy(Button button, Object data){
        Button result = new Button();
        result.setId(button.getId());
        result.setLabel(button.getLabel());
        result.setData(data);
        return result;
    }

    public List<List<Button>> getStartButtons() {
        List<List<Button>> markupInLine = new ArrayList<>();
        List<Button> rowsInLine = new ArrayList<>();
        markupInLine.add(getLineButton(start));
        markupInLine.add(getLineButton(cancel));
        return markupInLine;
    }

    public List<List<Button>> getEditButtons() {
        List<List<Button>> markupInLine = new ArrayList<>();
        List<Button> firstLine = new ArrayList<>();
        firstLine.add(changeName);
        firstLine.add(changeSquad);
        markupInLine.add(firstLine);
        List<Button> secondLine = new ArrayList<>();
        secondLine.add(changeAbout);
        secondLine.add(changeNickname);
        markupInLine.add(secondLine);
        markupInLine.add(getLineButton(back));
        return markupInLine;
    }

    public List<List<Button>> getHelpButtons() {
        List<List<Button>> rowsInLine = new ArrayList<>();
        rowsInLine.add(getLineButton(about));
        rowsInLine.add(getLineButton(showInfo));
        rowsInLine.add(getLineButton(edit));
        rowsInLine.add(getLineButton(status));
        rowsInLine.add(getLineButton(stop));
        rowsInLine.add(getLineButton(hideButtons));
        return rowsInLine;
    }

    public List<List<Button>> getForwardToHelpButtons() {

        List<List<Button>> rowsInLine = new ArrayList<>();
        List<Button> firstLine = new ArrayList<>();
        firstLine.add(back);
        firstLine.add(hideButtons);
        rowsInLine.add(firstLine);
        return rowsInLine;
    }

    public List<List<Button>> getAdminButtons() {
        List<List<Button>> rowsInLine = new ArrayList<>();
        rowsInLine.add(getLineButton(adminShowUsers));
        rowsInLine.add(getLineButton(adminShowInfo));
        rowsInLine.add(getLineButton(adminBanUser));
        rowsInLine.add(getLineButton(adminUnbanUser));
        return rowsInLine;
    }

    public List<List<Button>> getAdminUserInfo(User user) {
        List<List<Button>> rowsInLine = new ArrayList<>();
        List<Button> buttonsLine = new ArrayList<>();
        buttonsLine.add(adminBanUser);
        buttonsLine.add(adminUnbanUser);
        buttonsLine.add(hideButtons);
        rowsInLine.add(buttonsLine);
        return rowsInLine;
    }

    public List<List<Button>> getFeedbackButtons(Long pairId){
        List<List<Button>> rowsInLine = new ArrayList<>();
        rowsInLine.add(getLineButton(getCopy(feedBackSuccess, pairId)));
        rowsInLine.add(getLineButton(getCopy(feedBackFailed, pairId)));
        rowsInLine.add(getLineButton(getCopy(feedBackNotSure, pairId)));
        return rowsInLine;
    }

    public List<Button> getLineButton(Button button) {
        List<Button> buttonsLine = new ArrayList<>();
        buttonsLine.add(button);
        return buttonsLine;
    }



    public List<InlineKeyboardButton> getLineButton(InlineKeyboardButton button) {
        List<InlineKeyboardButton> buttonsLine = new ArrayList<>();
        buttonsLine.add(button);
        return buttonsLine;
    }
}
