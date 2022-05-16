package com.gamemanager.backend.appUserGame;

import com.gamemanager.backend.appUser.AppUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppUserGameService {

    private final AppUserGameRepository appUserGameRepository;

    public AppUserGame save(AppUserGame appUserGame) {
        return appUserGameRepository.save(appUserGame);
    }

    public void delete(Long id) {
        appUserGameRepository.deleteById(id);
    }

    public AppUserGame updateGame(AppUserGame appUserGame) {
        AppUserGame appUserGameToUpdate = appUserGameRepository.findById(appUserGame.getId()).orElseThrow(() -> new RuntimeException("Game not found"));
        if (appUserGame.getGameStatus() != null && appUserGame.getGameStatus() != appUserGameToUpdate.getGameStatus()) {
            appUserGameToUpdate.setGameStatus(appUserGame.getGameStatus());
        }
        if (appUserGame.getPlayTime() != null && appUserGame.getPlayTime() != appUserGameToUpdate.getPlayTime()) {
            appUserGameToUpdate.setPlayTime(appUserGame.getPlayTime());
        }

        return appUserGameToUpdate;

    }

    public AppUserGame deleteGame(Long gameId) {
        AppUserGame appUserGameToDelete = appUserGameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        AppUser appUser = appUserGameToDelete.getAppUser();
        appUser.getGames().remove(appUserGameToDelete);
        appUserGameRepository.delete(appUserGameToDelete);
        return appUserGameToDelete;
    }
}



