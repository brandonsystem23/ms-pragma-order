package com.pragma.order_service.infrastructure.security.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.test.StepVerifier;

class BearerTokenAuthenticationConverterTest {

    private final BearerTokenAuthenticationConverter converter = new BearerTokenAuthenticationConverter();

    @Test
    void shouldConvertBearerTokenSuccessfully() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-123")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(converter.convert(exchange))
                .assertNext(authentication -> {
                    Assertions.assertNotNull(authentication);
                    Assertions.assertEquals("token-123", authentication.getCredentials());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenAuthorizationHeaderIsMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(converter.convert(exchange))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenAuthorizationHeaderIsNotBearer() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic abc123")
                .build();

        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(converter.convert(exchange))
                .verifyComplete();
    }
}
