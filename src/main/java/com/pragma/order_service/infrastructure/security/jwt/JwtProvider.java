package com.pragma.order_service.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtProvider {

    private final String secret;
    private SecretKey secretKey;

    public JwtProvider(@Value("${security.jwt.secret}") String secret) {
        this.secret = secret;
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthenticatedUser validateAndGetUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return AuthenticatedUser.builder()
                .userId(claims.get("userId", Long.class))
                .fullName(claims.get("fullName", String.class))
                .role(claims.get("role", String.class))
                .numberDocument(claims.get("numberDocument", String.class))
                .phone(claims.get("phone", String.class))
                .email(claims.get("email", String.class))
                .build();
    }
}
