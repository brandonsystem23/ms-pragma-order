package com.pragma.order_service.domain.model.command;

import java.util.List;

public record CreateOrderCommand(

        Long restaurantId,
        List<CreateOrderItemCommand> items
) {
}
