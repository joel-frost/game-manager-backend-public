package com.gamemanager.backend.security.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AuthorisationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
       if (request.getServletPath().equals("/api/v1/login") || request.getServletPath().equals("/api/v1/appUser/refreshToken")
       || request.getServletPath().equals("/api/v1/appUser/create/user/")){
           // Pass to next filter in chain
           filterChain.doFilter(request, response);
       } else {;
           String authHeader = request.getHeader("Authorization");
           if (authHeader != null && authHeader.startsWith("Bearer ")) {
               try {
                   // Remove 'bearer' from header
                   String token = authHeader.substring(7);
                   // Decode token
                   Algorithm algorithm = SecurityUtilities.getAlgorithm();
                   JWTVerifier verifier = JWT.require(algorithm).build();
                   DecodedJWT jwt = verifier.verify(token);
                   // Get user details from token
                   String user = jwt.getSubject();
                   String[] roles = jwt.getClaim("roles").asArray(String.class);
                   // Create authorities
                   Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                   for (String role : roles) {
                       authorities.add(new SimpleGrantedAuthority(role));
                   }
                   // Generate a token for the user that provides the correct access
                   UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
                   // Send token to spring security
                   SecurityContextHolder.getContext().setAuthentication(authToken);
                   // Continue to next filter in chain
                   filterChain.doFilter(request, response);
               } catch (Exception e) {
                   //TODO: extract this to a util class
                   log.error("Error verifying token: {}", e.getMessage());
                   Map<String, String> errorResponse = new HashMap<>();
                   errorResponse.put("message", e.getMessage());
                   response.setContentType("application/json");
                   response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                   new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
               }
           } else {
               // Continue to next filter in chain
               filterChain.doFilter(request, response);
           }
       }

    }
}
