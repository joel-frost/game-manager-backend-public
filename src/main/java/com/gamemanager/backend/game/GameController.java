package com.gamemanager.backend.game;

import lombok.AllArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping(path="api/v1/game")
public class GameController {
    
    private final GameService gameService;

    
    @GetMapping
    public List<Game> getAllGames() { return gameService.getAllGames(); }

    @GetMapping(path="/search")
    public List<Game> searchGames(@RequestParam("searchTerm") String searchTerm) { return gameService.searchGames(searchTerm); }

    @PostMapping(path = "/steam")
    public void getSteamGames(@RequestBody String steamId) { gameService.getSteamGames(steamId); }


    @PostMapping
    public void registerNewGame(@RequestBody Game game) {
        gameService.addNewGame(game);
    }

    @DeleteMapping(path = "{gameId}")
    public void deleteGame(@PathVariable("gameId") Long gameId) {
        gameService.deleteGame(gameId);
    }

    @PutMapping
    public void updateGame(@RequestBody Game updatedGame) {
        gameService.updateGame(updatedGame);
    }
    
}
