package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import reactor.core.publisher.Mono;

public interface ListRestaurantsUseCase {

    Mono<PagedResponse<RestaurantListItemResponse>> list(String token, int page, int size);
}
