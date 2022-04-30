package com.gamemanager.backend.game;

import com.google.common.util.concurrent.RateLimiter;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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
public class GameService {

    private final GameRepository gameRepository;
    private final String clientId = System.getenv("CLIENT_ID");
    private final String clientSecret = System.getenv("CLIENT_SECRET");
    private final String steamKey = System.getenv("STEAM_KEY");
    private final RateLimiter rateLimiter = RateLimiter.create(4.0);
    private static final int MAX_RESULTS = 4;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public void getSteamGames(String steamId) {
        final String urlStr = "http://api.steampowered.com/IPlayerService/GetOwnedGames/v0001/?key="+ steamKey+ "&steamid="+ steamId +"&include_appinfo=1&format=json";

        JSONObject root = getJSONRoot(urlStr);
        JSONArray games = root.getJSONObject("response").getJSONArray("games");
        //for (int i = 0; i < games.length(); i++) {
        for (int i = 0; i < 10; i++) {
            // This will always be a JSONObject so casting is safe
            rateLimiter.acquire();
            JSONObject currentGame = (JSONObject) games.get(i);
            String gameName = currentGame.getString("name");
            storeGame(gameName);
        }
    }

    private JSONArray searchIGDB(String gameName) {
        String token = getIgdbAccessToken();
        String body = "search \"" + gameName + "\";\n" +
                "fields aggregated_rating,cover,summary,first_release_date,genres,name;";
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

    private Game convertToGameObject(JSONObject jsonGame) {
        String name = "Unknown";
        String summary = "No summary available";
        int cover = -1;
        float rating = -1.0f;
        LocalDate releaseDate = LocalDate.now();
        int genre = -1;
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
            releaseDate = Instant.ofEpochMilli(jsonGame.getInt("first_release_date")).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (jsonGame.has("genres")) {
            JSONArray genreArr = jsonGame.getJSONArray("genres");
            genre = genreArr.getInt(0);
        }
        return new Game(name, summary, cover, rating, releaseDate, genre);
    }

    private void storeGame(String gameName) {
        JSONArray jsonArr = searchIGDB(gameName);
        if (jsonArr == null || jsonArr.length() <= 0) {
            gameRepository.save(new Game(gameName, "No summary available", -1, -1.0f, LocalDate.now(), -1));
        }
        else {
            Game game = convertToGameObject(jsonArr.getJSONObject(0));
            gameRepository.save(game);
        }
    }

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

    @Transactional
    public void updateGame(Game updatedGame) {
        Game game = gameRepository.findById(updatedGame.getId()).orElseThrow(() ->
                new IllegalStateException(
                        "Game with id " + updatedGame.getId() + " does not exist"
                ));

        if (updatedGame != null && updatedGame.getName() != null && !Objects.equals(game.getName(), updatedGame.getName())) {
            if (updatedGame.getName().length() > 0) {
                game.setName(updatedGame.getName());
            }
        }

        if (updatedGame != null && updatedGame.getDescription() != null && !Objects.equals(game.getDescription(), updatedGame.getDescription())) {
            if (updatedGame.getDescription().length() > 0) {
                game.setDescription(updatedGame.getDescription());
            }
        }

        if (updatedGame != null && updatedGame.getGameStatus() != null && !Objects.equals(game.getGameStatus(), updatedGame.getGameStatus())) {
            if (updatedGame.getGameStatus().length() > 0) {
                game.setGameStatus(updatedGame.getGameStatus());
            }
        }

    }

    public List<Game> searchGames(String searchTerm) {
        //TODO: Check if exists first
        JSONArray results = searchIGDB(searchTerm);
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
