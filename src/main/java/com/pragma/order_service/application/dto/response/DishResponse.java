package com.pragma.order_service.application.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record DishResponse(

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
