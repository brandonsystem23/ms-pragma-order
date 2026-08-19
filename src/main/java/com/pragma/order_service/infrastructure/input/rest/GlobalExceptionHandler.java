package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException ex, ServerWebExchange exchange) {
        HttpStatus status = mapStatus(ex.getCode());
        return buildResponse(status, ex.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(
            ExternalServiceException ex,
            ServerWebExchange exchange
    ) {
        return buildResponse(ex.getStatus(), ex.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, ServerWebExchange exchange) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), exchange, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno en el servidor",
                exchange,
                List.of()
        );
    }

    private HttpStatus mapStatus(DomainErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR,
                 DUPLICATE_NIT,
                 DUPLICATE_NAME,
                 ACTIVE_ORDER_EXISTS,
                 INVALID_ORDER_RESTAURANT -> HttpStatus.BAD_REQUEST;

            case INVALID_TOKEN -> HttpStatus.UNAUTHORIZED;

            case OWNER_NOT_FOUND,
                 RESTAURANT_NOT_FOUND,
                 DISH_NOT_FOUND,
                 EMPLOYEE_RESTAURANT_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case INVALID_OWNER_ROLE,
                 ACCESS_DENIED,
                 INVALID_OWNER_RESTAURANT -> HttpStatus.FORBIDDEN;

            case INTERNAL_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            ServerWebExchange exchange,
            List<String> details
    ) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now(ZoneId.of("America/Lima")))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .details(details)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
