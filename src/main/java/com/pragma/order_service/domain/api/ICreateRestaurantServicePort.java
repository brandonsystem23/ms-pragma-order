package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import reactor.core.publisher.Mono;

public interface ICreateRestaurantServicePort {

    Mono<Restaurant> create(CreateRestaurantCommand createRestaurantCommand, String token);
}
