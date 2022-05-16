package com.gamemanager.backend.appUserGame;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.game.Game;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppUserGame {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne
    @JsonIgnore
    private AppUser appUser;
    @ManyToOne
    private Game game;
    private String gameStatus = "Not Set";
    private Integer playTime;

    public AppUserGame(AppUser appUser, Game game, int playTime) {
        this.appUser = appUser;
        this.game = game;
        this.playTime = playTime;
    }

}
