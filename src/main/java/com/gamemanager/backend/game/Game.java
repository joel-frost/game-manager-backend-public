package com.gamemanager.backend.game;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
@ToString
@Getter
@Setter
public class Game {
    @Id
    @SequenceGenerator(
            name = "game_sequence",
            sequenceName = "game_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "game_sequence"
    )
    private Long id;
    private String name;
    @Lob
    private String description;
    private int coverCode;
    private Float aggregatedRating;
    private LocalDate releaseDate;
    private String genre;
    private String gameStatus = "Not Set";
    private int playTime;
    private int steamAppId;

    public Game(String name) {
        this.name = name;
    }

    public Game(String name, String gameStatus) {
        this.name = name;
        this.gameStatus = gameStatus;
    }

    public Game(String name, String description, int coverCode, float aggregatedRating, LocalDate releaseDate, String genre, int playTime, int steamAppId) {
        this.name = name;
        this.description = description;
        this.coverCode = coverCode;
        this.aggregatedRating = aggregatedRating;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.playTime = playTime;
        this.steamAppId = steamAppId;
    }

}
