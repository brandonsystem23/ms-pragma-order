package com.pragma.order_service.infrastructure.output.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.domain.model.auth.AuthSession;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAuthSessionAdapterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RedisAuthSessionAdapter adapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldFindSessionByTokenSuccessfully() throws Exception {
        String token = "token-test";

        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        when(valueOperations.get(anyString())).thenReturn(Mono.just("{}"));
        when(objectMapper.readValue(anyString(), eq(AuthSession.class))).thenReturn(authSession);

        StepVerifier.create(adapter.findByToken(token))
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

        StepVerifier.create(adapter.findByToken(token))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenJsonDeserializationFails() throws Exception {
        String token = "token-test";

        when(valueOperations.get(anyString())).thenReturn(Mono.just("{}"));
        when(objectMapper.readValue(anyString(), eq(AuthSession.class)))
                .thenThrow(new JsonProcessingException("error") {});

        StepVerifier.create(adapter.findByToken(token))
                .expectError(IllegalStateException.class)
                .verify();
    }
}
