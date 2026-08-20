package com.pragma.order_service.infrastructure.output.redis.dto;

public record OrderPinRedisValue(
        String phoneNumber,
        String pin
) {
}

