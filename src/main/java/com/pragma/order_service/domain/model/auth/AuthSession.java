package com.pragma.order_service.domain.model.auth;

import lombok.Builder;

@Builder
public record AuthSession(

        Long userId,
        String fullName,
        String role,
        String numberDocument,
        String phone,
        String email
) {
}
