package com.pragma.order_service.application.dto.response;

import lombok.Builder;

@Builder
public record UpdateOrderResponse(
        Long id,
        String message
) {
}
