package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import reactor.core.publisher.Mono;

public interface CreateRestaurantUseCase {

    Mono<Restaurant> create(CreateRestaurantCommand command, String token);
}
