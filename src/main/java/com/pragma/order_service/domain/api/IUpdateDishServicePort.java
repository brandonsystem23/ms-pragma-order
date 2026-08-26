package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import reactor.core.publisher.Mono;

public interface IUpdateDishServicePort {

    Mono<Dish> update(Long dishId, UpdateDishCommand updateDishCommand, Long ownerId);

    Mono<Dish> updateStatus(Long dishId, Boolean status, Long ownerId);
}
