package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.mapper.DishDtoMapper;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.api.ICreateDishServicePort;
import com.pragma.order_service.domain.api.IListDishesServicePort;
import com.pragma.order_service.domain.api.IUpdateDishServicePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishHandlerTest {

    @Mock
    private ICreateDishServicePort createDishUseCase;

    @Mock
    private IUpdateDishServicePort updateDishUseCase;

    @Mock
    private DishDtoMapper dishDtoMapper;

    @Mock
    private IListDishesServicePort listDishesUseCase;

    @InjectMocks
    private DishHandler dishApplicationService;

    @Test
    void shouldCreateDishSuccessfully() {
        CreateDishRequest request = new CreateDishRequest(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25))
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
                .price(BigDecimal.valueOf(25))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(createDishUseCase.create(any(), anyString())).thenReturn(Mono.just(dish));
        when(dishDtoMapper.toCommand(any())).thenReturn(command);
        when(dishDtoMapper.toResponse(any(Dish.class))).thenReturn(dishResponse);

        StepVerifier.create(dishApplicationService.create(request, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals("Pizza Hawaiana", response.name());
                    Assertions.assertEquals(BigDecimal.valueOf(25), response.price());
                    Assertions.assertEquals(1L, response.restaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishSuccessfully() {
        UpdateDishRequest request = new UpdateDishRequest(
                BigDecimal.valueOf(20),
                "Descripción actualizada");

        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(20),
                "Descripción actualizada"
        );

        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(20))
                .description("Descripción actualizada")
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
                .price(BigDecimal.valueOf(20))
                .description("Descripción actualizada")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(updateDishUseCase.update(anyLong(), any(), anyString())).thenReturn(Mono.just(dish));
        when(dishDtoMapper.toUpdateCommand(any())).thenReturn(command);
        when(dishDtoMapper.toResponse(any(Dish.class))).thenReturn(dishResponse);

        StepVerifier.create(dishApplicationService.update(1L, request, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals(BigDecimal.valueOf(20), response.price());
                    Assertions.assertEquals("Descripción actualizada", response.description());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateStatusDishSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(20))
                .description("Descripción actualizada")
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
                .price(BigDecimal.valueOf(20))
                .description("Descripción actualizada")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(false)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(updateDishUseCase.updateStatus(anyLong(), any(), anyString())).thenReturn(Mono.just(dish));
        when(dishDtoMapper.toResponse(any(Dish.class))).thenReturn(dishResponse);

        StepVerifier.create(dishApplicationService.updateStatus(1L, false, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1L, response.id());
                    Assertions.assertEquals(BigDecimal.valueOf(20), response.price());
                    Assertions.assertEquals("Descripción actualizada", response.description());
                    Assertions.assertFalse(response.status());
                })
                .verifyComplete();
    }

    @Test
    void shouldListDishesByRestaurantSuccessfully() {
        PageResult<Dish> pageResult = PageResult.<Dish>builder()
                .content(List.of(
                        Dish.builder()
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
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        DishResponse dishResponse = DishResponse.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25))
                .description("Pizza con piña y jamón")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(listDishesUseCase.listByRestaurant(anyLong(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(Mono.just(pageResult));

        when(dishDtoMapper.toResponse(any(Dish.class))).thenReturn(dishResponse);

        StepVerifier.create(dishApplicationService.listByRestaurant(1L, "PIZZA", 0, 10, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals("Pizza Hawaiana", response.content().getFirst().name());
                    Assertions.assertEquals(1L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                })
                .verifyComplete();
    }
}
