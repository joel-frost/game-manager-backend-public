package com.gamemanager.backend.appUser;

import com.gamemanager.backend.appUserGame.AppUserGame;
import com.gamemanager.backend.game.Game;
import com.gamemanager.backend.game.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
//TODO: Validation
//TODO: Check public methods
public class AppUserService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final String steamKey = System.getenv("STEAM_KEY");
    private final GameRepository gameRepository;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public AppUser saveAppUser(AppUser appUser) {
        log.info("Saving appUser: {}", appUser);
        Optional<AppUser> optionalAppUser = Optional.ofNullable(appUserRepository.findByEmail(appUser.getEmail()));
        if (optionalAppUser.isPresent()) {
            log.error("AppUser already exists");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "AppUser already exists");
        }
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        return appUserRepository.save(appUser);
    }
    public Role saveRole(Role role) {
        log.info("Saving role: {}", role);
        return roleRepository.save(role);
    }
    public void addRoleToAppUser(String email, String roleName) {
        log.info("Adding role: {} to appUser: {}", roleName, email);
        AppUser appUser = appUserRepository.findByEmail(email);
        Role role = roleRepository.findByName(roleName);
        appUser.getRoles().add(role);
    }
    public AppUser findAppUserByEmail(String email) {
        log.info("Finding appUser by email: {}", email);
        return appUserRepository.findByEmail(email);
    }
    public List<AppUser> getUsers() {
        log.info("Getting all users");
        return appUserRepository.findAll();
    }

    public AppUser addGameToAppUser(String email, AppUserGame game) {
        log.info("Adding game: {} to appUser: {}", game, email);

        Optional<AppUser> optionalAppUser = Optional.ofNullable(appUserRepository.findByEmail(email));
        if (!optionalAppUser.isPresent()) {
            log.error("AppUser not found");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "AppUser not found");
        }
        Optional<Game> optionalGame = gameRepository.findGameByName(game.getGame().getName());
        if (!optionalGame.isPresent()) {
            gameRepository.save(game.getGame());
        }
        optionalAppUser.get().getGames().add(game);
        return optionalAppUser.get();
    }

    public void addGameToAppUser(AppUser appUser, AppUserGame game) {
        log.info("Adding game: {} to appUser: {}", game, appUser.getEmail());
        appUser.getGames().add(game);
    }

    public ArrayList<AppUserGame> getAppUserGames(String email) {
        log.info("Getting appUser: {} games", email);
        AppUser appUser = appUserRepository.findByEmail(email);
        return new ArrayList<AppUserGame>(appUser.getGames());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found {}", email);
            throw new UsernameNotFoundException(email);
        } else {
            log.info("User found {}", email);
        }
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
        return new User(user.getEmail(), user.getPassword(), authorities);
    }

    public AppUser updateAppUser(String email, AppUser appUser) {
        log.info("Updating appUser: {}", email);
        Optional<AppUser> optionalAppUser = Optional.ofNullable(appUserRepository.findByEmail(email));
        if (optionalAppUser.isPresent()) {
            AppUser user = optionalAppUser.get();
            if (appUser.getFirstName() != null && !appUser.getFirstName().isEmpty()) {
                user.setFirstName(appUser.getFirstName());
            }
            if (appUser.getLastName() != null && !appUser.getLastName().isEmpty()) {
                user.setLastName(appUser.getLastName());
            }
            if (appUser.getPassword() != null && !appUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(appUser.getPassword()));
            }
            if (appUser.getSteamId() != null && !appUser.getSteamId().isEmpty()) {
                user.setSteamId(appUser.getSteamId());
            }
            if (appUser.getSteamUsername() != null && !appUser.getSteamUsername().isEmpty()) {
                user.setSteamUsername(appUser.getSteamUsername());
            }
            return appUserRepository.save(user);
        }
        log.error("User not found {}", email);
        throw new UsernameNotFoundException(email);
    }

    public AppUser addSteamIdFromUsername(String steamUsername, String email) {
        Optional<AppUser> optionalAppUser = Optional.ofNullable(appUserRepository.findByEmail(email));
        if (!optionalAppUser.isPresent()) {
            log.error("User not found {}", email);
            throw new UsernameNotFoundException(email);
        }
        AppUser appUser = optionalAppUser.get();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://api.steampowered.com/ISteamUser/ResolveVanityURL/v0001/?key="+steamKey+"&vanityurl="+steamUsername))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject responseObject = new JSONObject(response.body());
            if (responseObject.getJSONObject("response").getInt("success") == 1) {
                appUser.setSteamId(responseObject.getJSONObject("response").getString("steamid"));
                appUser.setSteamUsername(steamUsername);
                return appUser;
            }
            throw new RuntimeException("Steam username not found");
        } catch (Exception e) {
            return null;
        }

    }

    public AppUser deleteGameFromAppUser(String email, Long gameId) {
        Optional<AppUser> optionalAppUser = Optional.ofNullable(appUserRepository.findByEmail(email));
        if (!optionalAppUser.isPresent()) {
            log.error("User not found {}", email);
            throw new UsernameNotFoundException(email);
        }
        AppUser appUser = optionalAppUser.get();
        appUser.getGames().remove(gameRepository.findById(gameId).get());
        return appUserRepository.save(appUser);
    }

}
