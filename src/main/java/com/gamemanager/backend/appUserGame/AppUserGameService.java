package com.gamemanager.backend.appUserGame;

import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.appUser.AppUserService;
import com.gamemanager.backend.game.Game;
import com.gamemanager.backend.game.GameRepository;
import com.gamemanager.backend.game.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AppUserGameService {

    private final AppUserGameRepository appUserGameRepository;
    private final AppUserService appUserService;
    private final GameService gameService;
    private final GameRepository gameRepository;

    public AppUserGame updateGame(AppUserGame appUserGame) {
        AppUserGame appUserGameToUpdate = appUserGameRepository.findById(appUserGame.getId()).orElseThrow(() -> new RuntimeException("Game not found"));
        if (appUserGame.getGameStatus() != null && appUserGame.getGameStatus() != appUserGameToUpdate.getGameStatus()) {
            appUserGameToUpdate.setGameStatus(appUserGame.getGameStatus());
        }
        if (appUserGame.getPlayTime() != null && appUserGame.getPlayTime() != appUserGameToUpdate.getPlayTime()) {
            appUserGameToUpdate.setPlayTime(appUserGame.getPlayTime());
        }
        log.info("Updated game: " + appUserGameToUpdate);

        return appUserGameToUpdate;

    }

    public AppUserGame deleteGame(Long gameId) {
        AppUserGame appUserGameToDelete = appUserGameRepository.findById(gameId).orElseThrow(() -> new RuntimeException("Game not found"));
        AppUser appUser = appUserGameToDelete.getAppUser();
        appUser.getGames().remove(appUserGameToDelete);
        appUserGameRepository.delete(appUserGameToDelete);
        log.info("Deleted game: " + appUserGameToDelete);
        return appUserGameToDelete;
    }

    public AppUserGame addGame(Long appUserId, Game game) {
        Optional<AppUser> appUser = Optional.ofNullable(appUserService.findAppUserById(appUserId));
        if (!appUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AppUser not found");
        }
        Optional<Game> gameToAdd = gameService.findGameByName(game.getName());

        // If the game already exists in the database, add it to the user's list of games
        if (gameToAdd.isPresent()) {
            AppUserGame appUserGame = new AppUserGame(appUser.get(), gameToAdd.get(), 0);
            appUserGameRepository.save(appUserGame);
            appUser.get().getGames().add(appUserGame);
            return appUserGame;
        }
        gameRepository.save(game);
        AppUserGame appUserGame = new AppUserGame(appUser.get(), game, 0);
        appUserGameRepository.save(appUserGame);
        appUser.get().getGames().add(appUserGame);
        log.info("Added game: " + appUserGame);
        return appUserGame;
    }
}



