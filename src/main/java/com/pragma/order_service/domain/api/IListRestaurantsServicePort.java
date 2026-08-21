package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
import reactor.core.publisher.Mono;

public interface IListRestaurantsServicePort {

    Mono<PageResult<RestaurantListItem>> list(String token, int page, int size);
}
