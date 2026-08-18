package com.pragma.order_service.application.dto.response;

import lombok.Builder;

@Builder
public record RestaurantListItemResponse(
        String name,
        String urlLogo
) {
}
