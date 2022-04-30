package com.gamemanager.backend.game;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.OCTOBER;
import static java.time.Month.SEPTEMBER;

@Configuration
public class GameConfig {
    
    // For testing purposes

    /*@Bean
    CommandLineRunner commandLineRunner(GameRepository repository) {
        return args -> {
            Game halo = new Game("Halo 3");
            Game reddead = new Game("Red Dead Redemption 2", GameStatus.PLAYING);

            repository.saveAll(List.of(halo, reddead));
        };
    }*/
    
}
