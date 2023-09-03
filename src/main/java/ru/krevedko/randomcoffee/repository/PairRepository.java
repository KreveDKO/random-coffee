package ru.krevedko.randomcoffee.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.krevedko.randomcoffee.model.Pair;

import java.util.List;

public interface PairRepository extends CrudRepository<Pair, Long> {
    @Query("SELECT p from pairs p where p.pairDate >= current_date")
    List<Pair> findDailyPairs();


}
