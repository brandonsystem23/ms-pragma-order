package com.pragma.order_service.infrastructure.util;

public final class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private TokenExtractor() {
    }

    public static String extract(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("Authorization header inválido");
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}
