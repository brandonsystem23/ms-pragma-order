package com.pragma.order_service.infrastructure.output.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.infrastructure.output.redis.dto.AuthSessionRedisValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RedisAuthSessionAdapter implements AuthSessionPort {

    private static final String PREFIX = "auth:token:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<AuthSessionRedisValue> findByToken(String token) {
        return redisTemplate.opsForValue()
                .get(buildKey(token))
                .flatMap(this::deserialize);
    }

    private String buildKey(String token) {
        return PREFIX + token;
    }

    private Mono<AuthSessionRedisValue> deserialize(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, AuthSessionRedisValue.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException("Error deserializando la sesión", e));
        }
    }
}
