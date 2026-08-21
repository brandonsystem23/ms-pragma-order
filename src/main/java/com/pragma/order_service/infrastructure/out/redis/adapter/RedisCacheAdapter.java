package com.pragma.order_service.infrastructure.out.redis.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.infrastructure.out.redis.dto.AuthSessionRedisValue;
import com.pragma.order_service.infrastructure.out.redis.dto.OrderPinRedisValue;
import com.pragma.order_service.infrastructure.out.redis.mapper.RedisRequestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements IRedisCachePort {

    private static final String PREFIX = "auth:token:";

    private static final String PREFIX_PIN = "notification:pin:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisRequestMapper redisRequestMapper;

    @Override
    public Mono<AuthSession> findByToken(String token) {
        String key = PREFIX + token;

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> deserialize(
                        json,
                        AuthSessionRedisValue.class,
                        "Error deserializando la sesión"
                ))
                .map(redisRequestMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmployeeDocumentAndPin(String numberDocument, String pin) {
        String key = PREFIX_PIN + numberDocument + pin;

        log.info("Buscar PIN CON KEY {}", key);

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(json -> deserialize(
                        json,
                        OrderPinRedisValue.class,
                        "Error deserializando el PIN de seguridad"
                ))
                .map(value -> pin.equals(value.pin()))
                .defaultIfEmpty(false);
    }


    private <T> Mono<T> deserialize(String json, Class<T> clazz, String errorMessage) {
        try {
            return Mono.just(objectMapper.readValue(json, clazz));
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException(errorMessage, e));
        }
    }
}
