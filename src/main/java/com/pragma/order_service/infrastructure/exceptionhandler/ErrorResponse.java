package com.pragma.order_service.infrastructure.exceptionhandler;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ErrorResponse(

        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
