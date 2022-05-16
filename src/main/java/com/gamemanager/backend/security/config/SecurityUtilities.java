package com.gamemanager.backend.security.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gamemanager.backend.appUser.AppUser;
import com.gamemanager.backend.appUser.AppUserService;

public final class SecurityUtilities {

    // Refresh token logic not working on frontend, once fixed should set access token to a new value
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 24 * 7; // 1 week
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 24 * 7; // 1 week
    private static final String JWT_SECRET = System.getenv("JWT_SECRET");
    public static final String FRONTEND_URL = "http://localhost:3000";

    // Sets algorithm to be used for creating and verifying JWT tokens
    public static Algorithm getAlgorithm() {
        return Algorithm.HMAC256(JWT_SECRET.getBytes());
    }

    // Creates a JWT token for the given user
    public static AppUser getUserFromToken(String refreshToken, AppUserService appUserService) {
        Algorithm algorithm = getAlgorithm();
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(refreshToken);
        String username = jwt.getSubject();

        return appUserService.findAppUserByEmail(username);
    }
}
