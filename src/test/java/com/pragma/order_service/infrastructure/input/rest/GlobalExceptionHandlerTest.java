package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/restaurants").build()
        );
    }

    @Test
    void shouldHandleValidationError() {
        DomainException ex = new DomainException(
                DomainErrorCode.VALIDATION_ERROR,
                "El campo name es obligatorio"
        );

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex, exchange);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("El campo name es obligatorio", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleInvalidToken() {
        DomainException ex = new DomainException(
                DomainErrorCode.INVALID_TOKEN,
                "Token inválido o expirado"
        );

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex, exchange);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Token inválido o expirado", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleOwnerNotFound() {
        DomainException ex = new DomainException(
                DomainErrorCode.OWNER_NOT_FOUND,
                "El propietario no existe"
        );

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex, exchange);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("El propietario no existe", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleAccessDenied() {
        DomainException ex = new DomainException(
                DomainErrorCode.ACCESS_DENIED,
                "No tienes permisos para crear restaurantes"
        );

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex, exchange);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("No tienes permisos para crear restaurantes", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleIllegalArgumentException() {
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(
                new IllegalArgumentException("Authorization header inválido"),
                exchange
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Authorization header inválido", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleGenericException() {
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(
                new RuntimeException("Unexpected error"),
                exchange
        );

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Ocurrió un error interno en el servidor", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleInternalError() {
        DomainException ex = new DomainException(
                DomainErrorCode.INTERNAL_ERROR,
                "Error interno de dominio"
        );

        ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex, exchange);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Error interno de dominio", Objects.requireNonNull(response.getBody()).message());
    }

    @Test
    void shouldHandleExternalServiceException() {
        ExternalServiceException ex = new ExternalServiceException(
                HttpStatus.NOT_FOUND,
                "El usuario no existe"
        );

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(ex, exchange);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("El usuario no existe", Objects.requireNonNull(response.getBody()).message());
    }
}