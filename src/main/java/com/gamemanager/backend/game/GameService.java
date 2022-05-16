package com.gamemanager.backend.game;

import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.appUser.AppUserService;
import com.gamemanager.backend.appUserGame.AppUserGame;
import com.gamemanager.backend.appUserGame.AppUserGameRepository;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import javax.transaction.Transactional;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final AppUserService appUserService;
    private final AppUserGameRepository appUserGameRepository;
    private final String clientId = System.getenv("CLIENT_ID");
    private final String clientSecret = System.getenv("CLIENT_SECRET");
    private final String steamKey = System.getenv("STEAM_KEY");
    private final RateLimiter rateLimiter = RateLimiter.create(4.0);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public void addSteamGamesToLibrary(String steamId, String userEmail) {
        // Check if user exists
        Optional<AppUser> appUser = Optional.ofNullable(appUserService.findAppUserByEmail(userEmail));
        if (!appUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found");
        }
        final String urlStr = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key="+ steamKey+ "&steamid="+ steamId +"&include_appinfo=1&format=json";
        // Get JSON from Steam API
        JSONObject root = getJSONRoot(urlStr);
        JSONArray games = root.getJSONObject("response").getJSONArray("games");

        //for (int i = 0; i < games.length(); i++) {
        for (int i = 0; i < 10; i++) {
            // This will always be a JSONObject so casting is safe
            rateLimiter.acquire();
            JSONObject currentGame = (JSONObject) games.get(i);
            String gameName = currentGame.getString("name");
            int playTime = currentGame.getInt("playtime_forever");
            int steamAppId = currentGame.getInt("appid");
            // Proceed to add game to library
            storeGame(gameName, playTime, steamAppId, appUser.get());
        }
    }

    private void storeGame(String gameName, int playTime, int steamAppId, AppUser appUser) {
        // Check if the game already exists, if so add it to the user's library if not already there
        Optional<Game> optionalGame = gameRepository.findGameBySteamAppId(steamAppId);
        if (optionalGame.isPresent()) {
            Collection<AppUserGame> games = appUser.getGames();
            for (AppUserGame game : games) {
                if (game.getGame().getSteamAppId() == steamAppId) {
                    log.info("Game already exists in user's list");
                    return;
                }
            }
            AppUserGame appUserGame = new AppUserGame(appUser, optionalGame.get(), playTime);
            appUserGameRepository.save(appUserGame);
            appUser.getGames().add(appUserGame);
            return;
        }

        // Get the game's information from the IGDB
        JSONArray jsonArr = getIGDBJsonArray(gameName);

        // If the game doesn't exist in IGDB, add it to the database
        if (jsonArr == null || jsonArr.length() <= 0) {
            Game game = new Game(gameName, "", -1, -1, null, "", steamAppId);
            gameRepository.save(game);
            AppUserGame appUserGame = new AppUserGame(appUser, game, playTime);
            appUserGameRepository.save(appUserGame);
            appUserService.addGameToAppUser(appUser, appUserGame);
        }
        // If the game was found in IGDB, add it to the database and add it to the user's library
        else {
            Game game = convertToGameObject(jsonArr.getJSONObject(0), playTime, steamAppId);
            gameRepository.save(game);
            AppUserGame appUserGame = new AppUserGame(appUser, game, playTime);
            appUserGameRepository.save(appUserGame);
            appUserService.addGameToAppUser(appUser, appUserGame);
        }
    }

    private JSONArray getIGDBJsonArray(String gameName) {
        String token = getIgdbAccessToken();
        String body = "search \"" + gameName + "\";\n" +
                "fields aggregated_rating,cover,summary,first_release_date,genres.name,name;";
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create("https://api.igdb.com/v4/games"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .setHeader("Client-ID", clientId)
                .setHeader("Authorization", "Bearer " + token)
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new JSONArray(response.body());
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method that runs if the game doesn't exist in IGDB
    private Game convertToGameObject(JSONObject jsonGame) {
        return convertToGameObject(jsonGame, -1, -1);
    }

    // Check the fields of the game and convert them to a Game object
    private Game convertToGameObject(JSONObject jsonGame, int playTime, int steamAppId) {
        System.out.println(jsonGame.toString());
        String name = "Unknown";
        String summary = "";
        int cover = -1;
        float rating = -1.0f;
        LocalDate releaseDate = null;
        String genre = "";
        if (jsonGame.has("name")) {
            name = jsonGame.getString("name");
        }
        if (jsonGame.has("summary")) {
            summary = jsonGame.getString("summary");
        }
        if (jsonGame.has("cover")) {
            cover = jsonGame.getInt("cover");
        }
        if (jsonGame.has("aggregated_rating")) {
            rating = jsonGame.getFloat("aggregated_rating");
        }
        if (jsonGame.has("first_release_date")) {
            releaseDate = Instant.ofEpochSecond(jsonGame.getLong("first_release_date")).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (jsonGame.has("genres")) {
            JSONArray genreArr = jsonGame.getJSONArray("genres");
            genre = genreArr.getJSONObject(0).getString("name");
        }
        return new Game(name, summary, cover, rating, releaseDate, genre, steamAppId);
    }

    // Tokens expire regularly, so need to get a new one on every request
    private String getIgdbAccessToken() {
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .uri(URI.create("https://id.twitch.tv/oauth2/token?client_id=" + clientId + "&client_secret=" + clientSecret + "&grant_type=client_credentials"))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonRes = new JSONObject(response.body());
            String token = jsonRes.getString("access_token");
            return token;
        } catch (Exception e) {
            System.out.println(e);
        }

        return "error";
    }

    // Helper method to get the root of the JSON from the URL
    private JSONObject getJSONRoot(String urlStr) {
        try {
            URL url = new URL(urlStr);
            System.out.println(urlStr);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String json = "";
            String line = "";
            while ((line = br.readLine()) != null) {
                json += line;
            }
            br.close();
            return new JSONObject(json);
        } catch (IOException e) {
            // TODO Handle exception
            e.printStackTrace();
        }
        throw new JSONException("Data loading error");
    }

    public void addNewGame(Game game) {
        Optional<Game> gameOptional = gameRepository.findById(game.getId());
        if (gameOptional.isPresent()) {
            throw new IllegalStateException("Game already exists");
        }
        gameRepository.save(game);
    }

    public void deleteGame(Long gameId) {
        boolean exists = gameRepository.existsById(gameId);
        if (!exists) {
            throw new IllegalStateException(("Game with id " + gameId + " does not exist"));
        }
        gameRepository.deleteById(gameId);
    }

    public List<Game> searchIGDBByGameName(String searchTerm) {
        //TODO: Check if exists first
        JSONArray results = getIGDBJsonArray(searchTerm);
        List<Game> resultGames = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            Game game = convertToGameObject(results.getJSONObject(i));
            resultGames.add(game);
        }
        //Sort the results by aggregated rating
        resultGames.sort((Game g1, Game g2) -> (int) Math.signum(g2.getAggregatedRating()-g1.getAggregatedRating()));
        return resultGames;
    }
}
