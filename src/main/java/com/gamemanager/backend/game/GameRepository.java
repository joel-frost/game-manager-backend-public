package com.gamemanager.backend.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findGameByName(String name);

    Optional<Game> findGameBySteamAppId(int steamAppId);

    Optional<Game> findByName(String name);

    List<Game> findByNameContainingIgnoreCase(String name);
}
