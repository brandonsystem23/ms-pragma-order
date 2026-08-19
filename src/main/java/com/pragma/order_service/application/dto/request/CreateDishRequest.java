package com.pragma.order_service.application.dto.request;

import java.math.BigDecimal;

public record CreateDishRequest(

        String name,
        BigDecimal price,
        String description,
        String urlImage,
        String category,
        Boolean status,
        Long restaurantId
) {
}
