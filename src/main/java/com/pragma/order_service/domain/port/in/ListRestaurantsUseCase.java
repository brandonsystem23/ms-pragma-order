package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
import reactor.core.publisher.Mono;

public interface ListRestaurantsUseCase {

    Mono<PageResult<RestaurantListItem>> list(String token, int page, int size);
}
