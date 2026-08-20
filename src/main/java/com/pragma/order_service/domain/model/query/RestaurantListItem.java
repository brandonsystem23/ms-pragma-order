package com.pragma.order_service.domain.model.query;

import lombok.Builder;

@Builder
public record RestaurantListItem(
        Long id,
        String name,
        String urlLogo
) {
}
