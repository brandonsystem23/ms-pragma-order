package com.pragma.order_service.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExternalServiceException extends RuntimeException {

    private final HttpStatus status;

    public ExternalServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}

