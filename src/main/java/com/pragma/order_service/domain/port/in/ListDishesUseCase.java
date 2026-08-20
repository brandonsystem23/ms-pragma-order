package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.query.DishQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import reactor.core.publisher.Mono;

public interface ListDishesUseCase {

    Mono<PageResult<DishQueryModel>> listByRestaurant(Long restaurantId, String category, int page, int size,
                                                      String token);
}
