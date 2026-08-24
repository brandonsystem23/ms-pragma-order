package com.pragma.order_service.domain.builder;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;

public final class DishBuilder {

    private DishBuilder() {

    }

    public static Dish buildToDish(CreateDishCommand createDishCommand) {
        return Dish.builder()
                .name(createDishCommand.name())
                .price(createDishCommand.price())
                .description(createDishCommand.description())
                .urlImage(createDishCommand.urlImage())
                .category(createDishCommand.category())
                .status(true)
                .restaurantId(createDishCommand.restaurantId())
                .build();
    }
}
