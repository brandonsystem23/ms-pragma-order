package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import reactor.core.publisher.Mono;

public interface ICreateDishServicePort {

    Mono<Dish> create(CreateDishCommand createDishCommand, String token);
}
