package ru.krevedko.randomcoffee.model;

import com.hubspot.jinjava.Jinjava;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import ru.krevedko.randomcoffee.config.Phrases;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;

@Data
@Entity(name = "users")
public class User {
    @Id
    private Long id;
    private boolean active = false;
    private String currentCommand;
    private String name;
    private String nickname;
    private String about;
    private String squad;
    private String editState;
    @ColumnDefault("false")
    private boolean dailySkip = false;
    @ColumnDefault("false")
    private boolean banned = false;
    @ColumnDefault("current_timestamp")
    private Timestamp registerDate;
    @ColumnDefault("false")
    private boolean admin = false;

    public User(Long id, String nickname) {
        this.id = id;
        this.nickname = nickname;
        registerDate = Timestamp.from(Instant.now());
    }

    public User() {
        registerDate = Timestamp.from(Instant.now());
    }

    public String getCurrentUserText(Phrases phrases) {
        return getCurrentUserText(phrases, false);
    }

    public String getCurrentUserText(Phrases phrases, boolean adminTemplate) {
        Jinjava jinjava = new Jinjava();
        HashMap<String, Object> context = new HashMap<>();

        context.put("id", id);
        context.put("is_banned", banned);
        context.put("register_date", registerDate);
        context.put("is_admin", admin);
        context.put("name", name);
        context.put("squad", squad);
        context.put("about", about);
        context.put("nickname", nickname);
        String messageTemplate = phrases.getCurrentData();
        if (adminTemplate)
            return jinjava.render(phrases.getAdminCurrentData(), context);
        return jinjava.render(phrases.getCurrentData(), context);
    }

    public String getPairText(Phrases phrases) {
        User user = this;
        String messageTemplate = phrases.getPairFound();
        if (about == null || about.isEmpty()) {
            messageTemplate = messageTemplate.replace("{about}", phrases.getNoneAbout());
        } else
            messageTemplate = messageTemplate.replace("{about}", user.getAbout());

        if (squad == null || squad.isEmpty()) {
            messageTemplate = messageTemplate.replace("{squad}", phrases.getNoneSquad());
        } else {
            messageTemplate = messageTemplate.replace("{squad}", squad);
        }

        if (name == null || name.isEmpty()) {
            messageTemplate = messageTemplate.replace("{name}", phrases.getNoneName());
        } else {
            messageTemplate = messageTemplate.replace("{name}", name);
        }
        messageTemplate = messageTemplate.replace("{nickname}", nickname);
        return messageTemplate;
    }
}
