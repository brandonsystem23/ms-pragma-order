package com.pragma.order_service.infrastructure.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenExtractorTest {

    @Test
    void shouldExtractTokenSuccessfully() {
        String token = TokenExtractor.extract("Bearer token-test");
        assertEquals("token-test", token);
    }

    @Test
    void shouldThrowWhenHeaderIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TokenExtractor.extract(null)
        );

        assertEquals("Authorization header inválido", ex.getMessage());
    }

    @Test
    void shouldThrowWhenHeaderDoesNotStartWithBearer() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> TokenExtractor.extract("Basic token-test")
        );

        assertEquals("Authorization header inválido", ex.getMessage());
    }
}
