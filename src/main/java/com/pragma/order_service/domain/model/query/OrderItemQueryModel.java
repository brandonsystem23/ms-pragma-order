package com.pragma.order_service.domain.model.query;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemQueryModel(
        Long dishId,
        String name,
        BigDecimal quantity
) {
}
