package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.mapper.DishDtoMapper;
import com.pragma.order_service.domain.api.ICreateDishServicePort;
import com.pragma.order_service.domain.api.IListDishesServicePort;
import com.pragma.order_service.domain.api.IUpdateDishServicePort;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.model.query.PageResult;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishHandlerTest {

    @Mock
    private ICreateDishServicePort createDishServicePort;

    @Mock
    private IUpdateDishServicePort updateDishServicePort;

    @Mock
    private IListDishesServicePort listDishesServicePort;

    @Mock
    private DishDtoMapper dishDtoMapper;

    @InjectMocks
    private DishHandler dishHandler;

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

        DishResponse response = DishResponse.builder()
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

        when(dishDtoMapper.toCommand(request)).thenReturn(command);
        when(createDishServicePort.create(command, 99L)).thenReturn(Mono.just(dish));
        when(dishDtoMapper.toResponse(dish)).thenReturn(response);

        StepVerifier.create(dishHandler.create(request, 99L))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals("Pizza Hawaiana", result.name());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishSuccessfully() {
        UpdateDishRequest request = new UpdateDishRequest(
                BigDecimal.valueOf(30),
                "Descripción actualizada"
        );

        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(30),
                "Descripción actualizada"
        );

        Dish dish = Dish.builder()
                .id(1L)
                .price(BigDecimal.valueOf(30))
                .description("Descripción actualizada")
                .build();

        DishResponse response = DishResponse.builder()
                .id(1L)
                .price(BigDecimal.valueOf(30))
                .description("Descripción actualizada")
                .build();

        when(dishDtoMapper.toUpdateCommand(request)).thenReturn(command);
        when(updateDishServicePort.update(1L, command, 99L)).thenReturn(Mono.just(dish));
        when(dishDtoMapper.toResponse(dish)).thenReturn(response);

        StepVerifier.create(dishHandler.update(1L, request, 99L))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals(BigDecimal.valueOf(30), result.price());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishStatusSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .status(false)
                .build();

        DishResponse response = DishResponse.builder()
                .id(1L)
                .status(false)
                .build();

        when(updateDishServicePort.updateStatus(1L, false, 99L)).thenReturn(Mono.just(dish));
        when(dishDtoMapper.toResponse(dish)).thenReturn(response);

        StepVerifier.create(dishHandler.updateStatus(1L, false, 99L))
                .assertNext(result -> Assertions.assertFalse(result.status()))
                .verifyComplete();
    }

    @Test
    void shouldListDishesByRestaurantSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .build();

        PageResult<Dish> pageResult = PageResult.<Dish>builder()
                .content(List.of(dish))
                .page(0)
                .size(10)
                .totalElements(1)
                .totalPages(1)
                .build();

        DishResponse response = DishResponse.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .build();

        when(listDishesServicePort.listByRestaurant(1L, "PIZZA", 0, 10))
                .thenReturn(Mono.just(pageResult));
        when(dishDtoMapper.toResponse(dish)).thenReturn(response);

        StepVerifier.create(dishHandler.listByRestaurant(1L, "PIZZA", 0, 10))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals("Pizza Hawaiana", result.content().getFirst().name());
                })
                .verifyComplete();
    }
}
