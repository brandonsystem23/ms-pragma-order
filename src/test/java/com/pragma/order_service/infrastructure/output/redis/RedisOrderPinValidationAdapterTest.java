package com.pragma.order_service.infrastructure.output.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.infrastructure.output.redis.dto.OrderPinRedisValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisOrderPinValidationAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisOrderPinValidationAdapter adapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnTrueWhenPinExists() throws Exception {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.just("{\"phoneNumber\":\"+573503124650\",\"pin\":\"151370\"}"));

        when(objectMapper.readValue(anyString(), eq(OrderPinRedisValue.class)))
                .thenReturn(new OrderPinRedisValue("+573503124650", "151370"));

        StepVerifier.create(adapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenKeyDoesNotExist() {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.empty());

        StepVerifier.create(adapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenPinDoesNotMatch() throws Exception {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.just("{\"phoneNumber\":\"+573503124650\",\"pin\":\"999999\"}"));

        when(objectMapper.readValue(anyString(), eq(OrderPinRedisValue.class)))
                .thenReturn(new OrderPinRedisValue("+573503124650", "999999"));

        StepVerifier.create(adapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenJsonDeserializationFails() throws Exception {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.just("{}"));

        when(objectMapper.readValue(anyString(), eq(OrderPinRedisValue.class)))
                .thenThrow(new JsonProcessingException("error") {});

        StepVerifier.create(adapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectError(IllegalStateException.class)
                .verify();
    }
}
