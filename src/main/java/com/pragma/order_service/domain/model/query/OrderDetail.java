package com.pragma.order_service.domain.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private Long restaurantId;
    private String restaurantName;
    private Long ownerId;
    private String status;
    private Long employeeAssignedId;
    private BigDecimal totalPrice;
    private Long dishId;
    private String dishName;
    private BigDecimal quantity;
    private BigDecimal dishPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
