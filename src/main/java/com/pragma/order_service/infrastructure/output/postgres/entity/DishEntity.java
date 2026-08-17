package com.pragma.order_service.infrastructure.output.postgres.entity;

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
@Table("dish")
public class DishEntity {

    @Id
    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    @Column("url_image")
    private String urlImage;

    private String category;

    private Boolean status;

    @Column("restaurant_id")
    private Long restaurantId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
