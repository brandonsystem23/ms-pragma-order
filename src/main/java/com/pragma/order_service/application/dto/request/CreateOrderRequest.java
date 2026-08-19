package com.pragma.order_service.application.dto.request;

import java.util.List;

public record CreateOrderRequest(

        Long restaurantId,
        List<CreateOrderItemRequest> items
) {
}

