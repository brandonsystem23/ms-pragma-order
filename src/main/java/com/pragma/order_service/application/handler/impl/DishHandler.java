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

    private final ICreateDishServicePort iCreateDishServicePort;
    private final IUpdateDishServicePort iUpdateDishServicePort;
    private final IListDishesServicePort iListDishesServicePort;
    private final DishDtoMapper dishDtoMapper;

    @Override
    public Mono<DishResponse> create(CreateDishRequest request, Long ownerId) {
        return iCreateDishServicePort.create(
                        dishDtoMapper.toCommand(request),
                        ownerId
                )
                .map(dishDtoMapper::toResponse);
    }

    @Override
    public Mono<DishResponse> update(Long dishId, UpdateDishRequest request, Long ownerId) {
        return iUpdateDishServicePort.update(
                        dishId,
                        dishDtoMapper.toUpdateCommand(request),
                        ownerId
                )
                .map(dishDtoMapper::toResponse);
    }

    @Override
    public Mono<DishResponse> updateStatus(Long dishId, Boolean status, Long ownerId) {
        return iUpdateDishServicePort.updateStatus(
                        dishId,
                        status,
                        ownerId
                )
                .map(dishDtoMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<DishResponse>> listByRestaurant(Long restaurantId, String category, int page, int size) {
        return iListDishesServicePort.listByRestaurant(restaurantId, category, page, size)
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
