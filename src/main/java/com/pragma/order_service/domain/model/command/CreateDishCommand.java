package com.pragma.order_service.domain.model.command;

import java.math.BigDecimal;

public record CreateDishCommand(

        String name,
        BigDecimal price,
        String description,
        String urlImage,
        String category,
        Boolean status,
        Long restaurantId
) {
}
