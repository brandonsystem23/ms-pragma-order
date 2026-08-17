package com.pragma.order_service.application.dto.request;

import java.math.BigDecimal;

public record UpdateDishRequest(

        BigDecimal price,
        String description
) {
}
