package ru.krevedko.randomcoffee.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "phrases")
public class Phrases {
    String firstQuestion;
    String start;
    String help;
    String about;
    String error;
    String cancelTemplate;
    String sucessStart;
    String firstEditSquad;
    String firstEditAbout;
    String currentData;
    String changeNameText;
    String changeAboutText;
    String changeSquadText;
    String changeNicknameText;
    String changeSuccess;
    String stopText;
    String pairFound;
    String statusText;
    String statusTextError;
    String feedBack;
    String feedBackSended;
    String noneAbout;
    String noneSquad;
    String noneName;
    String noneNickname;
    String noneNicknameUpdate;
    String noneInfo;

    String banSuccess;
    String unbanSuccess;
    String editError;

    String userBanned;
    String edit;
    private String adminPanel;
    private String adminEnterNickname;
    private String adminUserNotFound;
    private String adminCurrentData;
}
