package com.pragma.order_service.infrastructure.out.webclient.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateTraceabilityRequest(
        Long orderId,
        Long customerId,
        String customerName,
        Long restaurantId,
        String restaurantName,
        Long ownerRestaurant,
        Long employeeAssignedId,
        String employeeAssignedName,
        String status,
        String description,
        Long changedByUserId,
        String changedByRole,
        LocalDateTime changedAt
) {
}
