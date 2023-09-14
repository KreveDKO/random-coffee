package ru.krevedko.randomcoffee.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String configVersion;
    private String token;
    private String name;
    private String host;


}
