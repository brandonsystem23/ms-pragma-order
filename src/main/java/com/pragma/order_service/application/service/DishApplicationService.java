package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.mapper.DishDtoMapper;
import com.pragma.order_service.domain.port.in.CreateDishUseCase;
import com.pragma.order_service.domain.port.in.ListDishesUseCase;
import com.pragma.order_service.domain.port.in.UpdateDishUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DishApplicationService {

    private final CreateDishUseCase createDishUseCase;
    private final UpdateDishUseCase updateDishUseCase;
    private final ListDishesUseCase listDishesUseCase;
    private final DishDtoMapper dishDtoMapper;

    public Mono<DishResponse> create(CreateDishRequest request, String token) {
        return createDishUseCase.create(
                        dishDtoMapper.toCommand(request),
                        token
                )
                .map(dishDtoMapper::toResponse);
    }

    public Mono<DishResponse> update(Long dishId, UpdateDishRequest request, String token) {
        return updateDishUseCase.update(
                        dishId,
                        dishDtoMapper.toUpdateCommand(request),
                        token
                )
                .map(dishDtoMapper::toResponse);
    }

    public Mono<DishResponse> updateStatus(Long dishId, Boolean status, String token) {
        return updateDishUseCase.updateStatus(
                        dishId,
                        status,
                        token
                )
                .map(dishDtoMapper::toResponse);
    }

    public Mono<PagedResponse<DishResponse>> listByRestaurant(Long restaurantId, String category, int page, int size,
                                                              String token) {
        return listDishesUseCase.listByRestaurant(restaurantId, category, page, size, token);
    }
}
