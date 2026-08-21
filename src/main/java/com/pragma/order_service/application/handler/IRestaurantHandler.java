package com.pragma.order_service.application.handler;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import reactor.core.publisher.Mono;

public interface IRestaurantHandler {

    Mono<RestaurantResponse> create(CreateRestaurantRequest request, String token);

    Mono<PagedResponse<RestaurantListItemResponse>> list(String token, int page, int size);
}
