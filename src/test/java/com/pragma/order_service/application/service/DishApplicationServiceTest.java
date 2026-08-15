package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.mapper.DishDtoMapper;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.port.in.CreateDishUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishApplicationServiceTest {

    @Mock
    private CreateDishUseCase createDishUseCase;

    @Mock
    private DishDtoMapper dishDtoMapper;

    @InjectMocks
    private DishApplicationService dishApplicationService;

    @Test
    void shouldCreateDishSuccessfully() {
        CreateDishRequest request = new CreateDishRequest(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        DishResponse dishResponse = DishResponse.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(createDishUseCase.create(any(), anyString()))
                .thenReturn(Mono.just(dish));

        when(dishDtoMapper.toCommand(any())).thenReturn(command);
        when(dishDtoMapper.toResponse(any())).thenReturn(dishResponse);

        Mono<DishResponse> result = dishApplicationService.create(request, "token-test");

        StepVerifier.create(result)
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1L, response.id());
                    org.junit.jupiter.api.Assertions.assertEquals("Pizza Hawaiana", response.name());
                    org.junit.jupiter.api.Assertions.assertEquals(BigDecimal.valueOf(25000), response.price());
                    org.junit.jupiter.api.Assertions.assertEquals(1L, response.restaurantId());
                })
                .verifyComplete();
    }
}
