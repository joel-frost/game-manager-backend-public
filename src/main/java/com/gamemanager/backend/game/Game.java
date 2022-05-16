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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Lob
    private String description;
    private Integer coverCode;
    private Float aggregatedRating;
    private LocalDate releaseDate;
    private String genre;
    private Integer steamAppId;

    public Game(String name) {
        this.name = name;
    }
    public Game(String name, String description, int coverCode, float aggregatedRating, LocalDate releaseDate, String genre, int steamAppId) {
        this.name = name;
        this.description = description;
        this.coverCode = coverCode;
        this.aggregatedRating = aggregatedRating;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.steamAppId = steamAppId;
    }

}
