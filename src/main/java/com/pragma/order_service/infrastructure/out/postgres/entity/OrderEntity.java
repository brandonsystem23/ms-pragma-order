package com.pragma.order_service.infrastructure.out.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class OrderEntity {

    @Id
    private Long id;

    @Column("customer_id")
    private Long customerId;

    @Column("restaurant_id")
    private Long restaurantId;

    @Column("employee_assigned_id")
    private Long employeeAssignedId;

    private String status;

    @Column("total_price")
    private BigDecimal totalPrice;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
