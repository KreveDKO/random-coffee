package ru.krevedko.randomcoffee.model;

import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Data
@Entity(name = "pairs")
public class Pair {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    private Long leftUserId;
    private Long rightUserId;
    @ColumnDefault("current_timestamp")
    private Timestamp pairDate;
    private String feedBack;

    public Pair(){

    }

    public Pair(Long left, Long right){
        this.leftUserId = left;
        this.rightUserId = right;
        pairDate = Timestamp.from(Instant.now());
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
