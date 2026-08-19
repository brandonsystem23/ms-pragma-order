package com.pragma.order_service.infrastructure.output.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_item")
public class OrderItemEntity {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("dish_id")
    private Long dishId;

    private BigDecimal quantity;
}
