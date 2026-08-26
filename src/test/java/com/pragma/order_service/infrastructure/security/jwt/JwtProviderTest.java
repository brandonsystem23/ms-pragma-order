package com.pragma.order_service.infrastructure.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


class JwtProviderTest {

    @Test
    void shouldGenerateAndValidateTokenSuccessfully() {
        String secret = "my-super-secret-key-my-super-secret-key-123456";

        JwtProvider adapter = new JwtProvider(secret);
        adapter.init();

        byte[] keyBytes = Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8))
        );

        String token = Jwts.builder()
                .claim("userId", 7L)
                .claim("fullName", "Sofia Gomez")
                .claim("role", "EMPLEADO")
                .claim("numberDocument", "987654320")
                .claim("phone", "+573004445566")
                .claim("email", "sofia.gomez@plazoleta.com")
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();


        AuthenticatedUser decoded = adapter.validateAndGetUser(token);

        Assertions.assertEquals(7L, decoded.userId());
        Assertions.assertEquals("Sofia Gomez", decoded.fullName());
        Assertions.assertEquals("EMPLEADO", decoded.role());
        Assertions.assertEquals("987654320", decoded.numberDocument());
        Assertions.assertEquals("+573004445566", decoded.phone());
        Assertions.assertEquals("sofia.gomez@plazoleta.com", decoded.email());
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        String secret = "my-super-secret-key-my-super-secret-key-123456";

        JwtProvider adapter = new JwtProvider(secret);
        adapter.init();

        Assertions.assertThrows(Exception.class, () ->
                adapter.validateAndGetUser("token-invalido")
        );
    }
}
