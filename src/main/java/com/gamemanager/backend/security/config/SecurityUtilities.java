package com.gamemanager.backend.security.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.appUser.AppUserService;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

public final class SecurityUtilities {

    //TODO: Revert back to original time after refersh logic working on frontend
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 24 * 7; // 1 minute
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 24 * 7; // 1 week
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    public static final String FRONTEND_URL = "http://localhost:3000";

    public static Algorithm getAlgorithm() {
        return Algorithm.HMAC256(JWT_SECRET.getBytes());
    }

    public static AppUser getUserFromToken(String refreshToken, AppUserService appUserService) {
        Algorithm algorithm = getAlgorithm();
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(refreshToken);
        String username = jwt.getSubject();

        AppUser user = appUserService.findAppUserByEmail(username);

        return user;
    }
}
