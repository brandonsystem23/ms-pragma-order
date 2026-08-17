package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.service.DishApplicationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishControllerTest {

    @Mock
    private DishApplicationService dishApplicationService;

    @InjectMocks
    private DishController dishController;

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

        when(dishApplicationService.create(any(), anyString())).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.create("Bearer token-test", request))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals("Pizza Hawaiana", result.name());
                    Assertions.assertEquals(BigDecimal.valueOf(25000), result.price());
                    Assertions.assertEquals(1L, result.restaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateDishSuccessfully() {
        UpdateDishRequest request = new UpdateDishRequest(
                        BigDecimal.valueOf(30000),
                        "Descripción actualizada"
                );

        DishResponse response = DishResponse.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(30000))
                .description("Descripción actualizada")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(dishApplicationService.update(anyLong(), any(), anyString())).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.update(1L, "Bearer token-test", request))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.price());
                    Assertions.assertEquals("Descripción actualizada", result.description());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateStatusDishSuccessfully() {
        DishResponse response = DishResponse.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(30000))
                .description("Descripción actualizada")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(dishApplicationService.updateStatus(anyLong(), any(), anyString())).thenReturn(Mono.just(response));

        StepVerifier.create(dishController.updateStatus(1L, false,"Bearer token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.id());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.price());
                    Assertions.assertEquals("Descripción actualizada", result.description());
                })
                .verifyComplete();
    }

}
