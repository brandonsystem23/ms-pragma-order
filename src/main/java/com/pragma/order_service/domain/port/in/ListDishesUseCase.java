package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import reactor.core.publisher.Mono;

public interface ListDishesUseCase {

    Mono<PagedResponse<DishResponse>> listByRestaurant(Long restaurantId, String category, int page, int size,
                                                       String token);
}
