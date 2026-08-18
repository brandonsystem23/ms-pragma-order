package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.Restaurant;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RestaurantPersistencePort {

    Mono<Boolean> existsByNit(String nit);

    Mono<Restaurant> save(Restaurant restaurant);

    Mono<Boolean> existByOwner(Long restaurantId, Long ownerId);

    Mono<Boolean> existById(Long restaurantId);

    Flux<Restaurant> findActiveRestaurantsOrdered(int page, int size);

    Mono<Long> countActiveRestaurants();
}
