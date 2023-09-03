package ru.krevedko.randomcoffee.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.krevedko.randomcoffee.config.FileConfig;
import ru.krevedko.randomcoffee.config.Phrases;
import ru.krevedko.randomcoffee.model.User;
import ru.krevedko.randomcoffee.repository.PairRepository;
import ru.krevedko.randomcoffee.repository.UserRepository;
import ru.krevedko.randomcoffee.model.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
@EnableScheduling
public class SchedulerService {

    private final BotService botService;
    private final UserRepository userRepository;
    private final PairRepository pairRepository;
    private final Phrases phrases;
    private final FileConfig files;

    @Scheduled(cron = "0 0 9 * * *")
    public void foundPairWorker() {
        foundPair(false);
    }

    private void updateUserNickname() {
        log.debug("update nickname start");
        List<User> users = userRepository.getEmptyNickname();
        for (User user : users) {
            botService.sendMessage(user.getId(), phrases.getNoneNicknameUpdate());
        }
    }

    @SneakyThrows
    private void foundPair(boolean force) {
        log.debug("Sheduler task start");
        if (!force) {
            updateUserNickname();
            Thread.sleep(1000);
        }
        userRepository.updateNullNicknameUsers();
        List<User> users = userRepository.findNotPairedUsers();
        List<Long> existedUsers = new ArrayList<>();
        log.debug("Star founding pairs");
        int i = 0;
        for (User user : users) {
            if (existedUsers.stream().anyMatch(u -> u.equals(user.getId()))) continue;
            Optional<User> pairedUser = userRepository.findNewUser(user.getId()).stream().findFirst();
            if (!pairedUser.isPresent()) continue;
            User u = pairedUser.get();

            log.debug("user {} paired with {}", user, u);
            pairRepository.save(new Pair(user.getId(), u.getId()));
            pairRepository.save(new Pair(u.getId(), user.getId()));
            existedUsers.add(u.getId());
            File foundPairFile = new File(files.getFoundPair());
            if (foundPairFile.exists()) {
                botService.sendPhoto(user.getId(), foundPairFile, u.getPairText(phrases));
                botService.sendPhoto(pairedUser.get().getId(), foundPairFile, user.getPairText(phrases));
            } else {
                botService.sendMessage(user.getId(), u.getPairText(phrases));
                botService.sendMessage(pairedUser.get().getId(), user.getPairText(phrases));
            }
            if (i++ % 14 == 0) {
                Thread.sleep(1000);
            }
            ;
        }
        log.debug("Pairs founded");
    }

    @Scheduled(cron = "0 0 18 * * *")
    @SneakyThrows
    public void feedbackWorker() {
        log.debug("get feedback start");
        List<Pair> pairs = pairRepository.findDailyPairs();
        for (Pair pair : pairs) {
            User user = userRepository.findById(pair.getRightUserId()).get();
            File feedBackFile = new File(files.getFeedback());
            if (feedBackFile.exists()) {

            } else {
                botService.sendFeedBack(pair.getLeftUserId(), pair.getId(), user.getNickname());
            }
        }
    }

}
