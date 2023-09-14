package ru.krevedko.randomcoffee.service;

import ru.krevedko.randomcoffee.model.Button;

import java.io.File;
import java.util.List;

public interface IBotService {
    void sendPhoto(Long chatId, File file, String caption);

    void sendMessage(Long chatId, String text);

    void sendMessage(Long chatId, String text, List<List<Button>> keyboard);
}
