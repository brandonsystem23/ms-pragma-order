package com.pragma.order_service.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.infrastructure.exceptionhandler.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneId.of("America/Lima")))
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("No autenticado o token inválido")
                .path(exchange.getRequest().getPath().value())
                .details(List.of())
                .build();

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(writeValueAsBytes(response));

        return exchange.getResponse()
                .writeWith(Mono.just(buffer)
        );
    }

    private byte[] writeValueAsBytes(ErrorResponse response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            return """
                    {"status":401,"error":"Unauthorized","message":"No autenticado o token inválido"}
                    """.getBytes();
        }
    }
}