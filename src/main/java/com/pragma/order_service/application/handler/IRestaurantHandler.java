package com.pragma.order_service.application.handler;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import reactor.core.publisher.Mono;

public interface IRestaurantHandler {

    Mono<RestaurantResponse> create(CreateRestaurantRequest request, String token);

    Mono<PagedResponse<RestaurantListResponse>> list(int page, int size);
}
