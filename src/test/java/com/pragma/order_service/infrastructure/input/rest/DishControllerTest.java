package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IDishHandler;
import com.pragma.order_service.infrastructure.security.jwt.AuthenticatedUser;
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
class DishControllerTest {

    @Mock
    private IDishHandler dishHandler;

    @InjectMocks
    private DishController dishController;

    @Test
    void shouldCreateDishSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        CreateDishRequest request = new CreateDishRequest(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DishResponse response = DishResponse.builder()
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

        when(dishHandler.create(request, 2L)).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.create(user, request))
                .assertNext(result -> Assertions.assertEquals(1L, result.id()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        UpdateDishRequest request = new UpdateDishRequest(BigDecimal.valueOf(30000), "Actualizada");

        DishResponse response = DishResponse.builder()
                .id(1L)
                .price(BigDecimal.valueOf(30000))
                .description("Actualizada")
                .build();

        when(dishHandler.update(1L, request, 2L)).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.update(1L, user, request))
                .assertNext(result -> Assertions.assertEquals(BigDecimal.valueOf(30000), result.price()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishStatusSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        DishResponse response = DishResponse.builder()
                .id(1L)
                .status(false)
                .build();

        when(dishHandler.updateStatus(1L, false, 2L)).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.updateStatus(1L, false, user))
                .assertNext(result -> Assertions.assertFalse(result.status()))
                .verifyComplete();
    }

    @Test
    void shouldListDishesByRestaurantSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        PagedResponse<DishResponse> response = PagedResponse.<DishResponse>builder()
                .content(List.of(
                        DishResponse.builder()
                                .id(1L)
                                .name("Pizza Hawaiana")
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(dishHandler.listByRestaurant(1L, "PIZZA", 0, 10)).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.listByRestaurant(user, 1L, "PIZZA", 0, 10))
                .assertNext(result -> Assertions.assertEquals(1, result.content().size()))
                .verifyComplete();
    }
}
