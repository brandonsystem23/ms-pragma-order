package com.pragma.order_service.application.handler;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import reactor.core.publisher.Mono;

public interface IDishHandler {

    Mono<DishResponse> create(CreateDishRequest request, Long ownerId);

    Mono<DishResponse> update(Long dishId, UpdateDishRequest request, Long ownerId);

    Mono<DishResponse> updateStatus(Long dishId, Boolean status, Long ownerId);

    Mono<PagedResponse<DishResponse>> listByRestaurant(Long restaurantId, String category, int page, int size);
}
