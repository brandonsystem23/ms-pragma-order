package com.pragma.order_service.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(

        Long dishId,
        String name,
        BigDecimal quantity
) {
}
