package com.pragma.order_service.domain.model.command;

public record UpdateOrderCommand(
        String status,
        String pin
) {
}
