package com.pragma.order_service.domain.model.command;

public record CreateRestaurantCommand(

        String name,
        String nit,
        String address,
        String phone,
        String urlLogo,
        Long ownerId
) {
}
