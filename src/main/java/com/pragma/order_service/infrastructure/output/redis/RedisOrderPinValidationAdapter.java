package com.pragma.order_service.infrastructure.output.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.domain.port.out.OrderPinValidationPort;
import com.pragma.order_service.infrastructure.output.redis.dto.OrderPinRedisValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@RequiredArgsConstructor
@Slf4j
public class RedisOrderPinValidationAdapter implements OrderPinValidationPort {

    private static final String PREFIX = "notification:pin:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Boolean> existsByEmployeeDocumentAndPin(String numberDocument, String pin) {
        String key = PREFIX + numberDocument + pin;
        log.info("Buscar PIN CON KEY {}", key );

        return redisTemplate.opsForValue()
                .get(key)
                .flatMap(this::deserialize)
                .map(value -> pin.equals(value.pin()))
                .defaultIfEmpty(false);
    }

    private Mono<OrderPinRedisValue> deserialize(String json) {
        try {
            return Mono.just(objectMapper.readValue(json, OrderPinRedisValue.class));
        } catch (JsonProcessingException e) {
            return Mono.error(new IllegalStateException("Error deserializando el PIN de seguridad", e));
        }
    }
}
