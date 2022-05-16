package com.gamemanager.backend.appUserGame;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserGameRepository extends JpaRepository<AppUserGame, Long> {
    AppUserGame findByGameName(String gameName);
}
