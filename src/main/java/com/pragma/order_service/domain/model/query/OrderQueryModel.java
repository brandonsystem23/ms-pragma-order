package com.pragma.order_service.domain.model.query;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderQueryModel(
        Long id,
        Long customerId,
        String nameCustomer,
        Long restaurantId,
        String nameRestaurant,
        String status,
        Long employeeAssignedId,
        BigDecimal totalPrice,
        List<OrderItemQueryModel> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
