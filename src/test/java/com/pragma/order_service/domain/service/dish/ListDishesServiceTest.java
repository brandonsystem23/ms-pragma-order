package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.service.dish.validation.DishRetrieveValidator;
import com.pragma.order_service.domain.service.dish.validation.ListDishesDomainValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDishesServiceTest {

    @Mock
    private DishPersistencePort dishPersistencePort;

    @Mock
    private DishRetrieveValidator dishRetrieveValidator;

    @Mock
    private ListDishesDomainValidator listDishesDomainValidator;

    @InjectMocks
    private ListDishesService service;

    @Test
    void shouldListDishesSuccessfullyWithoutCategory() {
        Dish dish1 = Dish.builder()
                .id(1L)
                .name("Burger")
                .price(BigDecimal.valueOf(20000))
                .description("Burger clásica")
                .urlImage("https://image.com/burger.png")
                .category("FASTFOOD")
                .status(true)
                .restaurantId(1L)
                .build();

        Dish dish2 = Dish.builder()
                .id(2L)
                .name("Pizza")
                .price(BigDecimal.valueOf(30000))
                .description("Pizza familiar")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();
        doNothing().when(listDishesDomainValidator).validate(anyLong(), any(), anyInt(), anyInt());
        when(dishRetrieveValidator.validate(anyString(), anyLong())).thenReturn(Mono.empty());
        when(dishPersistencePort.findActiveByRestaurantId(1L, 0, 10)).thenReturn(Flux.just(dish1, dish2));
        when(dishPersistencePort.countActiveByRestaurantId(1L)).thenReturn(Mono.just(2L));

        StepVerifier.create(service.listByRestaurant(1L, null, 0, 10, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(2, response.content().size());
                    Assertions.assertEquals(0, response.page());
                    Assertions.assertEquals(10, response.size());
                    Assertions.assertEquals(2L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                    Assertions.assertEquals("Burger", response.content().getFirst().name());
                })
                .verifyComplete();
    }

    @Test
    void shouldListDishesSuccessfullyWithCategory() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza Hawaiana")
                .price(BigDecimal.valueOf(25000))
                .description("Pizza con piña")
                .urlImage("https://image.com/pizza.png")
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();
        doNothing().when(listDishesDomainValidator).validate(anyLong(), any(), anyInt(), anyInt());
        when(dishRetrieveValidator.validate(anyString(), anyLong())).thenReturn(Mono.empty());
        when(dishPersistencePort.findActiveByRestaurantIdAndCategory(1L, "PIZZA", 0, 10))
                .thenReturn(Flux.just(dish));
        when(dishPersistencePort.countActiveByRestaurantIdAndCategory(1L, "PIZZA"))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(service.listByRestaurant(1L, "PIZZA", 0, 10, "token-test"))
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals("Pizza Hawaiana", response.content().getFirst().name());
                    Assertions.assertEquals("PIZZA", response.content().getFirst().category());
                    Assertions.assertEquals(1L, response.totalElements());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenNoDishesExist() {
        when(dishRetrieveValidator.validate(anyString(), anyLong())).thenReturn(Mono.empty());
        when(dishPersistencePort.findActiveByRestaurantId(1L, 0, 10)).thenReturn(Flux.empty());
        when(dishPersistencePort.countActiveByRestaurantId(1L)).thenReturn(Mono.just(0L));
        doNothing().when(listDishesDomainValidator).validate(anyLong(), any(), anyInt(), anyInt());

        StepVerifier.create(service.listByRestaurant(1L, null, 0, 10, "token-test"))
                .assertNext(response -> {
                    Assertions.assertTrue(response.content().isEmpty());
                    Assertions.assertEquals(0L, response.totalElements());
                    Assertions.assertEquals(0, response.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {
        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenThrow(new DomainException(null, "El restaurante no existe") {});

        doNothing().when(listDishesDomainValidator).validate(anyLong(), any(), anyInt(), anyInt());

        StepVerifier.create(service.listByRestaurant(1L, null, 0, 10, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals(
                            "El restaurante no existe",
                            error.getMessage()
                    );
                })
                .verify();
    }
}
