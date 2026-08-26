package com.pragma.order_service.infrastructure.security.jwt;

import lombok.Builder;

@Builder
public record AuthenticatedUser(
        Long userId,
        String fullName,
        String role,
        String numberDocument,
        String phone,
        String email
) {
}
