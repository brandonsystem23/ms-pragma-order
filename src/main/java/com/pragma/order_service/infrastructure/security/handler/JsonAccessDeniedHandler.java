package com.pragma.order_service.infrastructure.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.order_service.infrastructure.exceptionhandler.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneId.of("America/Lima")))
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message("No tienes permisos para acceder a este recurso")
                .path(exchange.getRequest().getPath().value())
                .details(List.of())
                .build();

        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(writeValueAsBytes(response));

        return exchange.getResponse()
                .writeWith(Mono.just(buffer));

    }

    private byte[] writeValueAsBytes(ErrorResponse response) {
        try {
            return objectMapper.writeValueAsBytes(response);
        } catch (Exception e) {
            return """
                    {"status":403,"error":"Forbidden","message":"No tienes permisos para acceder a este recurso"}
                    """.getBytes();
        }
    }
}