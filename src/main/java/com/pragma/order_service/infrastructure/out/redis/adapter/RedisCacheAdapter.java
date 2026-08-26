package com.pragma.order_service.infrastructure.out.redis.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.infrastructure.out.redis.dto.OrderPinRedisValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisCacheAdapter implements IRedisCachePort {

    private static final String PREFIX_PIN = "notification:pin:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Boolean> existsByEmployeeDocumentAndPin(String numberDocument, String pin) {
        String key = PREFIX_PIN + numberDocument + pin;

        log.info("Buscando PIN con key={}", key);

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(this::deserializeOrderPin)
                .map(value -> pin.equals(value.pin()))
                .defaultIfEmpty(false);
    }

    private Mono<OrderPinRedisValue> deserializeOrderPin(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, OrderPinRedisValue.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException("Error deserializando el PIN de seguridad", e));
        }
    }
}
