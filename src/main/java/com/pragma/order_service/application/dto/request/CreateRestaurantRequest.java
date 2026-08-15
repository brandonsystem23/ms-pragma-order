package com.pragma.order_service.application.dto.request;

public record CreateRestaurantRequest(

        String name,
        String nit,
        String address,
        String phone,
        String urlLogo,
        Long ownerId
) {
}
