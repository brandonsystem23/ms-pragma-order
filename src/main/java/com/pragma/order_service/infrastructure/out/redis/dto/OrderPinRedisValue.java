package com.pragma.order_service.infrastructure.out.redis.dto;

public record OrderPinRedisValue(
        String phoneNumber,
        String pin
) {
}

