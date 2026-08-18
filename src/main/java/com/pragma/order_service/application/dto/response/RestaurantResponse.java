package com.pragma.order_service.application.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RestaurantResponse(

        Long id,
        String name,
        String nit,
        String address,
        String phone,
        String urlLogo,
        Long ownerId,
        Boolean status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
