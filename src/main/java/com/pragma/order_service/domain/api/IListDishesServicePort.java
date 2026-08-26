package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.query.PageResult;
import reactor.core.publisher.Mono;

public interface IListDishesServicePort {

    Mono<PageResult<Dish>> listByRestaurant(Long restaurantId, String category, int page, int size);
}
