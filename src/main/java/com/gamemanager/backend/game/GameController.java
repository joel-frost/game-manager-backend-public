package com.gamemanager.backend.game;

import lombok.AllArgsConstructor;
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
    public List<Game> searchGames(@RequestParam("searchTerm") String searchTerm) { return gameService.searchIGDBByGameName(searchTerm); }

    @PostMapping(path = "/steam/{steamId}/{userEmail}")
    public void getSteamGames(@PathVariable String steamId, @PathVariable String userEmail) { gameService.addSteamGamesToLibrary(steamId, userEmail); }


    @PostMapping
    public void registerNewGame(@RequestBody Game game) {
        gameService.addNewGame(game);
    }

}
