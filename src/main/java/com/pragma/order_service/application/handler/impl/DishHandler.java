package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IDishHandler;
import com.pragma.order_service.application.mapper.DishDtoMapper;
import com.pragma.order_service.domain.api.ICreateDishServicePort;
import com.pragma.order_service.domain.api.IListDishesServicePort;
import com.pragma.order_service.domain.api.IUpdateDishServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DishHandler implements IDishHandler {

    private final ICreateDishServicePort createDishServicePort;
    private final IUpdateDishServicePort updateDishServicePort;
    private final IListDishesServicePort listDishesServicePort;
    private final DishDtoMapper dishDtoMapper;

    @Override
    public Mono<DishResponse> create(CreateDishRequest request, Long ownerId) {
        return createDishServicePort.create(
                        dishDtoMapper.toCommand(request),
                        ownerId
                )
                .map(dishDtoMapper::toResponse);
    }

    @Override
    public Mono<DishResponse> update(Long dishId, UpdateDishRequest request, Long ownerId) {
        return updateDishServicePort.update(
                        dishId,
                        dishDtoMapper.toUpdateCommand(request),
                        ownerId
                )
                .map(dishDtoMapper::toResponse);
    }

    @Override
    public Mono<DishResponse> updateStatus(Long dishId, Boolean status, Long ownerId) {
        return updateDishServicePort.updateStatus(
                        dishId,
                        status,
                        ownerId
                )
                .map(dishDtoMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<DishResponse>> listByRestaurant(Long restaurantId, String category, int page, int size) {
        return listDishesServicePort.listByRestaurant(restaurantId, category, page, size)
                .map(result -> PagedResponse.<DishResponse>builder()
                        .content(result.content().stream()
                                .map(dishDtoMapper::toResponse)
                                .toList())
                        .page(result.page())
                        .size(result.size())
                        .totalElements(result.totalElements())
                        .totalPages(result.totalPages())
                        .build());
    }
}
