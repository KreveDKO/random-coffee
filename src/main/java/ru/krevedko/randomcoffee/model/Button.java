package ru.krevedko.randomcoffee.model;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

@Data
public class Button {
    String id;
    String label;
    Object data = null;

    public InlineKeyboardButton toTelegramButton(){
        InlineKeyboardButton.InlineKeyboardButtonBuilder builder = InlineKeyboardButton.builder();
        builder.text(this.getLabel())
                .callbackData(this.getId());
        if (data != null)
            builder.callbackData(id+ ";"+ data);
        return builder.build();
    }
    public InlineKeyboardButton toTelegramButton(Object additionalData){
        return InlineKeyboardButton.builder()
                .text(this.getLabel())
                .callbackData(this.getId()+ ";"+ additionalData)
                .build();
    }
}
