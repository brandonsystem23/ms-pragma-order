package com.pragma.order_service.infrastructure.security.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.infrastructure.exceptionhandler.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.access.AccessDeniedException;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JsonAccessDeniedHandler jsonAccessDeniedHandler;

    MockServerHttpRequest request;

    MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
       request = MockServerHttpRequest.get("/api/test").build();
       exchange = MockServerWebExchange.from(request);
    }

    @Test
    void shouldWriteForbiddenResponse() throws JsonProcessingException {

        when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());

        StepVerifier.create(jsonAccessDeniedHandler.handle(exchange, new AccessDeniedException("Forbidden")))
                .verifyComplete();

    }

    @Test
    void shouldUseFallbackResponseWhenObjectMapperFails() throws JsonProcessingException {

        when(objectMapper.writeValueAsBytes(any(ErrorResponse.class)))
                .thenThrow(new JsonProcessingException("Error serializando") {});

        StepVerifier.create(jsonAccessDeniedHandler.handle(exchange, new AccessDeniedException("Forbidden")))
                .verifyComplete();

    }
}