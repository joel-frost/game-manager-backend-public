package com.gamemanager.backend.appUser;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemanager.backend.appUserGame.AppUserGame;
import com.gamemanager.backend.security.config.SecurityUtilities;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("DuplicatedCode")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appUser")
public class AppUserController {
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<AppUser>> getUsers() {
        return ResponseEntity.ok().body(appUserService.getUsers());
    }

    @GetMapping("/findByEmail/{email}")
    public ResponseEntity<AppUser> findByEmail(@PathVariable String email, Principal principal) {
        if (principal == null || !principal.getName().equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to view this page");
        }
        return ResponseEntity.ok().body(appUserService.findAppUserByEmail(email));
    }

    @PostMapping("/addSteamIdFromUsername/{steamUsername}/{email}")
    public ResponseEntity<AppUser> findSteamId(@PathVariable String steamUsername, @PathVariable String email) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/appUser/addSteamIdFromUsername/{steamUsername}").buildAndExpand(steamUsername).toUriString());
        return ResponseEntity.created(uri).body(appUserService.addSteamIdFromUsername(steamUsername, email));
    }


    @GetMapping("/games/{email}")
    public ResponseEntity<Collection<AppUserGame>> getGames(@PathVariable String email, Principal principal) {
        if (principal == null || !principal.getName().equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to view this page");
        }
        return ResponseEntity.ok().body(appUserService.getAppUserGames(email));
    }

    @PutMapping("/update/{email}")
    public ResponseEntity<AppUser> updateAppUser(@PathVariable String email, @RequestBody AppUser appUser, Principal principal) {
        if (principal == null || !principal.getName().equals(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to view this page");
        }
        return ResponseEntity.ok().body(appUserService.updateAppUser(email, appUser));
    }

    @PostMapping("/create/user")
    public ResponseEntity<AppUser> createUser(@RequestBody AppUser appUser) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/appUser/create").toUriString());
        return ResponseEntity.created(uri).body(appUserService.saveAppUser(appUser));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/appUser/role/save").toUriString());
        return ResponseEntity.created(uri).body(appUserService.saveRole(role));
    }

    @PostMapping("/addRole")
    public ResponseEntity<?> addRoleToAppUser(@RequestBody RoleToUser roleToUser) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/appUser/addRole").toUriString());
        appUserService.addRoleToAppUser(roleToUser.getUsername(), roleToUser.getRoleName());
        return ResponseEntity.created(uri).build();
    }
    @GetMapping("/refreshToken")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String refreshToken = authHeader.substring(7);
                Algorithm algorithm = SecurityUtilities.getAlgorithm();
                AppUser user = SecurityUtilities.getUserFromToken(refreshToken, appUserService);

                String accessToken = JWT.create().withSubject(user.getEmail())
                        .withExpiresAt(new Date(System.currentTimeMillis() + SecurityUtilities.ACCESS_TOKEN_EXPIRATION_TIME))
                        .withIssuer(request.getRequestURL().toString())
                        //TODO make this easier to read
                        .withClaim("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
                        .sign(algorithm);

                Map<String, String> tokens = new HashMap<>();
                tokens.put("access-token", accessToken);
                tokens.put("refresh-token", refreshToken);
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
            } catch (Exception e) {

                log.error("Error verifying token: {}", e.getMessage());
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", e.getMessage());
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided");
        }
    }
}

@Data
class RoleToUser {
    private String username;
    private String roleName;
}
