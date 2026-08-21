package com.pragma.order_service.application.dto.request;

public record UpdateOrderRequest(
        String status,
        String pin
) {
}
