package ru.krevedko.randomcoffee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.krevedko.randomcoffee.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select  u from users u where u.nickname = ?1")
    Optional<User> getUserByNick(String nickname);

    @Query("SELECT u from users u where u.id != ?1 and u.banned = false and u.id not in (select p.rightUserId from pairs p where p.leftUserId = ?1) and u.id not in (select p.rightUserId from pairs p where p.pairDate >= current_date) and u.active = true order by random()")
    List<User> findNewUser(Long userId);

    @Query("select u.nickname from users u where u.active = true and u.admin = true")
    List<String> getAdminsNickname();

    @Query("SELECT u from users u where u.active = true and u.banned = false and u.id not in (select p.rightUserId from pairs p where p.pairDate > current_date )")
    List<User> findNotPairedUsers();

    @Modifying
    @Transactional
    @Query("UPDATE users u set u.active = false where u.nickname is null or u.nickname = ''")
    void updateNullNicknameUsers();

    @Query("SELECT u from users u where u.id in (select p.rightUserId from pairs p where p.pairDate > current_date and p.leftUserId = ?1)")
    Optional<User> findDailyPair(Long userId);

    @Query("select  u from users u where u.nickname is null and u.active = true")
    List<User> getEmptyNickname();
}
