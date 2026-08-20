package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.infrastructure.output.redis.dto.AuthSessionRedisValue;
import reactor.core.publisher.Mono;

public interface AuthSessionPort {

    Mono<AuthSessionRedisValue> findByToken(String token);
}
