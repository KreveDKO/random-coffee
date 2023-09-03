package ru.krevedko.randomcoffee.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.krevedko.randomcoffee.config.Phrases;
import ru.krevedko.randomcoffee.model.User;
import ru.krevedko.randomcoffee.repository.UserRepository;
import ru.krevedko.randomcoffee.exception.NullNicknameException;
import ru.krevedko.randomcoffee.model.Pair;
import ru.krevedko.randomcoffee.repository.PairRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PairRepository pairRepository;
    private final Phrases phrases;

    public User getOrCreateUser(Message message) throws NullNicknameException {
        Long id = message.getFrom().getId();
        if (message.getFrom().getUserName() == null || message.getFrom().getUserName().isEmpty()) {
            throw new NullNicknameException();
        }
        Optional<User> userOpt = userRepository.findById(message.getFrom().getId());
        User user;
        if (userOpt.isEmpty()) {
            user = new User(id, message.getFrom().getUserName());
            userRepository.save(user);
        } else {
            user = userOpt.get();
        }

        return user;
    }

    public void updateCommand(Long userId, String command){

        userRepository.findById(userId).ifPresent(user -> {
            user.setCurrentCommand(command);
            userRepository.save(user);
        });
    }


    public String statusMessage(long chatId) {
        Optional<User> user = userRepository.findDailyPair(chatId);
        if (user.isEmpty()) {
            return phrases.getStatusTextError();
        }
        return phrases.getStatusText().replace("{nickname}", user.get().getNickname());

    }

    public void saveFeedback(String feedBackData, String args) {
        Pair pair = pairRepository.findById(Long.parseLong(args)).get();
        pair.setFeedBack(feedBackData);
        pairRepository.save(pair);
    }

    public boolean setBanUser(String nickname, boolean isBanned) {
        Optional<User> user = userRepository.getUserByNick(nickname);
        if (user.isEmpty()) return false;
        user.ifPresent(u -> {
            u.setBanned(isBanned);
            userRepository.save(u);
        });
        return true;
    }
}
