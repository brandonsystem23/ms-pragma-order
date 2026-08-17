package com.pragma.order_service.domain.model.command;

import java.math.BigDecimal;

public record UpdateDishCommand(

        BigDecimal price,
        String description
) {
}
