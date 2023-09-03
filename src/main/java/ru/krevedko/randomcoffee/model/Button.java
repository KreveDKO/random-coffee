package ru.krevedko.randomcoffee.model;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Data
public class Button {
    String id;
    String label;

    public InlineKeyboardButton toTelegramButton(){
        return InlineKeyboardButton.builder()
                .text(this.getLabel())
                .callbackData(this.getId())
                .build();
    }
    public InlineKeyboardButton toTelegramButton(Object additionalData){
        return InlineKeyboardButton.builder()
                .text(this.getLabel())
                .callbackData(this.getId()+ ";"+ additionalData)
                .build();
    }
}
