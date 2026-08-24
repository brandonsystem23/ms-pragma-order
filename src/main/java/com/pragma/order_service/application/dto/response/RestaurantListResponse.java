package com.pragma.order_service.application.dto.response;

import lombok.Builder;

@Builder
public record RestaurantListResponse(
        Long id,
        String name,
        String urlLogo
) {
}
