package ru.krevedko.randomcoffee.model;

import lombok.Data;
import ru.krevedko.randomcoffee.constant.CallbackStatus;

import java.util.List;

@Data
public class ServiceCallback {
    private final CallbackStatus status;
    private final String message;
    private final List<List<Button>> buttons;

    private ServiceCallback(CallbackStatus status, String message, List<List<Button>> buttons) {
        this.message = message;
        this.buttons = buttons;
        this.status = status;
    }

    public static ServiceCallback sendMessageCallback(String message, List<List<Button>> buttons){
        return new ServiceCallback(CallbackStatus.SEND_MESSAGE,  message, buttons);
    }

    public static ServiceCallback sendMessageCallback(String message){
        return sendMessageCallback(message, null);
    }

    public static ServiceCallback updateMessageCallback(String message, List<List<Button>> buttons){
        return new ServiceCallback(CallbackStatus.UPDATE_MESSAGE,  message, buttons);
    }

    public static ServiceCallback updateMessageCallback( String message){
        return updateMessageCallback(message, null);
    }

    public static ServiceCallback defaultCallback(){
        return new ServiceCallback(CallbackStatus.NONE, null, null);
    }
}
