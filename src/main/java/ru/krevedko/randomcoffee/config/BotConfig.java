package ru.krevedko.randomcoffee.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String configVersion;
    private String token;
    private String name;

    public String getAdminsString(List<String> admins){
        return admins.stream().map(c -> "@" + c).collect(Collectors.joining(" "));
    }
}
