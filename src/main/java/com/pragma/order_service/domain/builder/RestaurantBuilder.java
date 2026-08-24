package com.pragma.order_service.domain.builder;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.model.query.RestaurantListItem;

public final class RestaurantBuilder {

    private RestaurantBuilder() {

    }

    public static Restaurant buildRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        return Restaurant.builder()
                .name(createRestaurantCommand.name())
                .nit(createRestaurantCommand.nit())
                .address(createRestaurantCommand.address())
                .phone(createRestaurantCommand.phone())
                .urlLogo(createRestaurantCommand.urlLogo())
                .ownerId(createRestaurantCommand.ownerId())
                .status(true)
                .build();
    }

    public static RestaurantListItem buildRestaurantListItem(Restaurant restaurant) {
        return RestaurantListItem.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .urlLogo(restaurant.getUrlLogo())
                .build();
    }
}
