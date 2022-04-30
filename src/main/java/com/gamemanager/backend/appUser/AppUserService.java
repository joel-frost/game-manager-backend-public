package com.gamemanager.backend.appUser;

import com.gamemanager.backend.game.Game;
import com.gamemanager.backend.game.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    private final GameRepository gameRepository;

    public AppUser saveAppUser(AppUser appUser) {
        log.info("Saving appUser: {}", appUser);
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

    public void addGameToAppUser(String email, String gameName) {
        log.info("Adding game: {} to appUser: {}", gameName, email);
        AppUser appUser = appUserRepository.findByEmail(email);
        Optional<Game> game = gameRepository.findGameByName(gameName);
        if (game.isPresent()) {
            appUser.getGames().add(game.get());
        } else {
            log.error("Game not found");
        }
    }

    public Collection<Game> getAppUserGames(String email) {
        log.info("Getting appUser: {} games", email);
        AppUser appUser = appUserRepository.findByEmail(email);
        return appUser.getGames();
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
}
