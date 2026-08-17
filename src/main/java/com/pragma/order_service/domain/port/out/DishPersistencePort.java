package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.Dish;
import reactor.core.publisher.Mono;

public interface DishPersistencePort {

    Mono<Boolean> existsByName(String name);

    Mono<Dish> save(Dish dish);

    Mono<Dish> findById(Long dishId);
}
