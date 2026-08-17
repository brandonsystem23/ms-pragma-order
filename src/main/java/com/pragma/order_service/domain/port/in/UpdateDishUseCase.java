package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import reactor.core.publisher.Mono;

public interface UpdateDishUseCase {

    Mono<Dish> update(Long dishId, UpdateDishCommand command, String token);
}

