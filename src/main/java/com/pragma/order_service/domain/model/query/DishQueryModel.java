package com.pragma.order_service.domain.model.query;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DishQueryModel(
        Long id,
        String name,
        BigDecimal price,
        String description,
        String urlImage,
        String category,
        Boolean status,
        Long restaurantId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
