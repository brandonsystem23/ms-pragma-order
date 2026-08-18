package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.mapper.RestaurantDtoMapper;
import com.pragma.order_service.domain.port.in.CreateRestaurantUseCase;
import com.pragma.order_service.domain.port.in.ListRestaurantsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RestaurantApplicationService {

    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final ListRestaurantsUseCase listRestaurantsUseCase;
    private final RestaurantDtoMapper restaurantDtoMapper;

    public Mono<RestaurantResponse> create(CreateRestaurantRequest request, String token) {
        return createRestaurantUseCase.create(
                        restaurantDtoMapper.toCommand(request),
                        token
                )
                .map(restaurantDtoMapper::toResponse);
    }

    public Mono<PagedResponse<RestaurantListItemResponse>> list(String token, int page, int size) {
        return listRestaurantsUseCase.list(token, page, size);
    }
}
