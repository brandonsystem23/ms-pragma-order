package com.pragma.order_service.domain.exception;

public enum DomainErrorCode {
    VALIDATION_ERROR,
    DUPLICATE_NIT,
    OWNER_NOT_FOUND,
    INVALID_OWNER_ROLE,
    INVALID_TOKEN,
    ACCESS_DENIED,
    INTERNAL_ERROR
}
