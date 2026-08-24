package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.handler.IRestaurantHandler;
import com.pragma.order_service.application.mapper.RestaurantDtoMapper;
import com.pragma.order_service.domain.api.ICreateRestaurantServicePort;
import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestaurantHandler implements IRestaurantHandler {

    private final ICreateRestaurantServicePort iCreateRestaurantServicePort;
    private final IListRestaurantsServicePort iListRestaurantsServicePort;
    private final RestaurantDtoMapper restaurantDtoMapper;

    @Override
    public Mono<RestaurantResponse> create(CreateRestaurantRequest request, String token) {
        return iCreateRestaurantServicePort.create(
                        restaurantDtoMapper.toCommand(request),
                        token
                )
                .map(restaurantDtoMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<RestaurantListResponse>> list(String token, int page, int size) {
        return iListRestaurantsServicePort.list(token, page, size)
                .map(result -> PagedResponse.<RestaurantListResponse>builder()
                        .content(result.content().stream()
                                .map(restaurantDtoMapper::toResponse)
                                .toList())
                        .page(result.page())
                        .size(result.size())
                        .totalElements(result.totalElements())
                        .totalPages(result.totalPages())
                        .build());
    }
}
