package com.pragma.order_service.application.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(

        Long id,
        Long customerId,
        String nameCustomer,
        Long restaurantId,
        String nameRestaurant,
        String status,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
