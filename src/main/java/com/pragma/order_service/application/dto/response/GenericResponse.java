package com.pragma.order_service.application.dto.response;

import lombok.Builder;

@Builder
public record GenericResponse(
        Long id,
        String message
) {
}
