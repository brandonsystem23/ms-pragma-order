package com.pragma.order_service.infrastructure.output.redis.dto;

import lombok.Builder;

@Builder
public record AuthSessionRedisValue(

        Long userId,
        String fullName,
        String role,
        String numberDocument,
        String phone,
        String email
) {
}
