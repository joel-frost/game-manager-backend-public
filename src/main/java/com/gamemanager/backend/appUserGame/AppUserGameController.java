package com.gamemanager.backend.appUserGame;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/v1/appUserGame")
public class AppUserGameController {

    private AppUserGameService appUserGameService;

    @PutMapping("/updateGame")
    public ResponseEntity<AppUserGame> updateGame(@RequestBody AppUserGame appUserGame) {
        return ResponseEntity.ok(appUserGameService.updateGame(appUserGame));
    }

    @DeleteMapping("/deleteGame/{appUserGameId}")
    public ResponseEntity<AppUserGame> deleteGame(@PathVariable Long appUserGameId) {
        return ResponseEntity.ok(appUserGameService.deleteGame(appUserGameId));
    }

}
