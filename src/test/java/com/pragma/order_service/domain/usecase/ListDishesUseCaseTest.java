package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.validation.dish.DishRetrieveValidator;
import com.pragma.order_service.domain.validation.dish.ListDishesDomainValidator;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListDishesUseCaseTest {

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private DishRetrieveValidator dishRetrieveValidator;

    @Mock
    private ListDishesDomainValidator listDishesDomainValidator;

    @InjectMocks
    private ListDishesUseCase service;

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

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantId(
                anyLong(),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.just(dish1, dish2));

        when(dishPersistencePort.countActiveByRestaurantId(
                anyLong()
        )).thenReturn(Mono.just(2L));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                null,
                                0,
                                10,
                                "token-test"
                        )
                )
                .assertNext(response -> {
                    Assertions.assertEquals(2, response.content().size());
                    Assertions.assertEquals(0, response.page());
                    Assertions.assertEquals(10, response.size());
                    Assertions.assertEquals(2L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                    Assertions.assertEquals(
                            "Burger",
                            response.content().getFirst().getName()
                    );
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

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantIdAndCategory(
                anyLong(),
                anyString(),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.just(dish));

        when(dishPersistencePort.countActiveByRestaurantIdAndCategory(
                anyLong(),
                anyString()
        )).thenReturn(Mono.just(1L));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                "PIZZA",
                                0,
                                10,
                                "token-test"
                        )
                )
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals(
                            "Pizza Hawaiana",
                            response.content().getFirst().getName()
                    );
                    Assertions.assertEquals(
                            "PIZZA",
                            response.content().getFirst().getCategory()
                    );
                    Assertions.assertEquals(
                            1L,
                            response.totalElements()
                    );
                    Assertions.assertEquals(1, response.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldListDishesSuccessfullyWhenCategoryIsBlank() {

        Dish dish = Dish.builder()
                .id(1L)
                .name("Burger")
                .price(BigDecimal.valueOf(20000))
                .description("Burger clásica")
                .urlImage("https://image.com/burger.png")
                .category("FASTFOOD")
                .status(true)
                .restaurantId(1L)
                .build();

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantId(
                anyLong(),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.just(dish));

        when(dishPersistencePort.countActiveByRestaurantId(
                anyLong()
        )).thenReturn(Mono.just(1L));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                "   ",
                                0,
                                10,
                                "token-test"
                        )
                )
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals(
                            "Burger",
                            response.content().getFirst().getName()
                    );
                    Assertions.assertEquals(1L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenNoDishesExist() {

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantId(
                anyLong(),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.empty());

        when(dishPersistencePort.countActiveByRestaurantId(
                anyLong()
        )).thenReturn(Mono.just(0L));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                null,
                                0,
                                10,
                                "token-test"
                        )
                )
                .assertNext(response -> {
                    Assertions.assertTrue(response.content().isEmpty());
                    Assertions.assertEquals(
                            0L,
                            response.totalElements()
                    );
                    Assertions.assertEquals(
                            0,
                            response.totalPages()
                    );
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {

        DomainException exception =
                new DomainException(
                        null,
                        "El restaurante no existe"
                ) {
                };

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                null,
                                0,
                                10,
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertEquals(
                                "El restaurante no existe",
                                error.getMessage()
                        )
                )
                .verify();
    }

    @Test
    void shouldFailWhenDomainValidationFails() {

        DomainException exception =
                new DomainException(
                        null,
                        "Los parámetros de paginación no son válidos"
                ) {
                };

        doThrow(exception)
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                null,
                                0,
                                10,
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertEquals(
                                "Los parámetros de paginación no son válidos",
                                error.getMessage()
                        )
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenFindingDishesFails() {

        RuntimeException exception =
                new RuntimeException("Error obteniendo platos");

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantId(
                anyLong(),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.error(exception));

        when(dishPersistencePort.countActiveByRestaurantId(
                anyLong()
        )).thenReturn(Mono.just(0L));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                null,
                                0,
                                10,
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenCountingDishesFails() {

        RuntimeException exception =
                new RuntimeException("Error contando platos");

        Dish dish = Dish.builder()
                .id(1L)
                .name("Burger")
                .price(BigDecimal.valueOf(20000))
                .description("Burger clásica")
                .urlImage("https://image.com/burger.png")
                .category("FASTFOOD")
                .status(true)
                .restaurantId(1L)
                .build();

        doNothing()
                .when(listDishesDomainValidator)
                .validate(anyLong(), any(), anyInt(), anyInt());

        when(dishRetrieveValidator.validate(anyString(), anyLong()))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantId(
                anyLong(),
                anyInt(),
                anyInt()
        )).thenReturn(Flux.just(dish));

        when(dishPersistencePort.countActiveByRestaurantId(
                anyLong()
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        service.listByRestaurant(
                                1L,
                                null,
                                0,
                                10,
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertSame(exception, error)
                )
                .verify();
    }
}