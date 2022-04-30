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
    private float aggregatedRating;
    private LocalDate releaseDate;
    private int genreCode;
    private String gameStatus = "Not Set";

    public Game(String name) {
        this.name = name;
    }

    public Game(String name, String gameStatus) {
        this.name = name;
        this.gameStatus = gameStatus;
    }

    public Game(String name, String description, int coverCode, float aggregatedRating, LocalDate releaseDate, int genreCode) {
        this.name = name;
        this.description = description;
        this.coverCode = coverCode;
        this.aggregatedRating = aggregatedRating;
        this.releaseDate = releaseDate;
        this.genreCode = genreCode;
    }

}
