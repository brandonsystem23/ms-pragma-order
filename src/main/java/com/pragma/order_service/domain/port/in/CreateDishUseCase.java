package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import reactor.core.publisher.Mono;

public interface CreateDishUseCase {

    Mono<Dish> create(CreateDishCommand command, String token);
}
