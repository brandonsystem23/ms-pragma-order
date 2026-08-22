package com.pragma.order_service.domain.spi;

import com.pragma.order_service.domain.model.Dish;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IDishPersistencePort {

    Mono<Boolean> existsByName(String name);

    Mono<Dish> save(Dish dish);

    Mono<Dish> findByIdAndStatusTrue(Long dishId);

    Mono<Dish> findById(Long dishId);

    Flux<Dish> findByIds(List<Long> dishIds);

    Flux<Dish> findActiveByRestaurantId(Long restaurantId, int page, int size);

    Flux<Dish> findActiveByRestaurantIdAndCategory(Long restaurantId, String category, int page, int size);

    Mono<Long> countActiveByRestaurantId(Long restaurantId);

    Mono<Long> countActiveByRestaurantIdAndCategory(Long restaurantId, String category);
}
