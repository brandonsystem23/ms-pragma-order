package com.pragma.order_service.domain.model;

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
public class Dish {
    private Long id;

    private String name;

    private BigDecimal price;

    private String description;

    private String urlImage;

    private String category;

    private Boolean status;

    private Long restaurantId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
