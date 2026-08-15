package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.mapper.DishDtoMapper;
import com.pragma.order_service.domain.port.in.CreateDishUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class DishApplicationService {

    private final CreateDishUseCase createDishUseCase;
    private final DishDtoMapper dishDtoMapper;

    public Mono<DishResponse> create(CreateDishRequest request, String token) {
        return createDishUseCase.create(
                        dishDtoMapper.toCommand(request),
                        token
                )
                .map(dishDtoMapper::toResponse);
    }
}
