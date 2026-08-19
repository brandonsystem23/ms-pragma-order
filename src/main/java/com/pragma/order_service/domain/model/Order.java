package com.pragma.order_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    private Long customerId;

    private Long restaurantId;

    private String status;

    private List<OrderItem> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
