package com.pragma.order_service.infrastructure.out.redis.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.infrastructure.out.redis.dto.AuthSessionRedisValue;
import com.pragma.order_service.infrastructure.out.redis.dto.OrderPinRedisValue;
import com.pragma.order_service.infrastructure.out.redis.mapper.RedisRequestMapper;
import org.junit.jupiter.api.Assertions;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisRequestMapper redisRequestMapper;

    @InjectMocks
    private RedisCacheAdapter redisCacheAdapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldFindSessionByTokenSuccessfully() throws Exception {
        String token = "token-test";

        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        AuthSession session = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        when(valueOperations.get(anyString())).thenReturn(Mono.just("{}"));
        when(objectMapper.readValue(anyString(), eq(AuthSessionRedisValue.class))).thenReturn(authSession);

        when(redisRequestMapper.toDomain(any())).thenReturn(session);

        StepVerifier.create(redisCacheAdapter.findByToken(token))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.userId());
                    Assertions.assertEquals("ADMINISTRADOR", result.role());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenTokenDoesNotExist() {
        String token = "token-test";

        when(valueOperations.get(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(redisCacheAdapter.findByToken(token))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenJsonDeserializationFails() throws Exception {
        String token = "token-test";

        when(valueOperations.get(anyString())).thenReturn(Mono.just("{}"));
        when(objectMapper.readValue(anyString(), eq(AuthSessionRedisValue.class)))
                .thenThrow(new JsonProcessingException("error") {});

        StepVerifier.create(redisCacheAdapter.findByToken(token))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void shouldReturnTrueWhenPinExists() throws Exception {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.just("{\"phoneNumber\":\"+573503124650\",\"pin\":\"151370\"}"));

        when(objectMapper.readValue(anyString(), eq(OrderPinRedisValue.class)))
                .thenReturn(new OrderPinRedisValue("+573503124650", "151370"));

        StepVerifier.create(redisCacheAdapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenKeyDoesNotExist() {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.empty());

        StepVerifier.create(redisCacheAdapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenPinDoesNotMatch() throws Exception {
        when(valueOperations.get("notification:pin:12345678151370"))
                .thenReturn(Mono.just("{\"phoneNumber\":\"+573503124650\",\"pin\":\"999999\"}"));

        when(objectMapper.readValue(anyString(), eq(OrderPinRedisValue.class)))
                .thenReturn(new OrderPinRedisValue("+573503124650", "999999"));

        StepVerifier.create(redisCacheAdapter.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .expectNext(false)
                .verifyComplete();
    }

}
