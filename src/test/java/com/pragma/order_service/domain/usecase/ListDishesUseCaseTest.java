package com.pragma.order_service.domain.usecase;

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

import static org.mockito.Mockito.doNothing;
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
        Dish dish = Dish.builder()
                .id(1L)
                .name("Burger")
                .price(BigDecimal.valueOf(20000))
                .restaurantId(1L)
                .build();

        doNothing().when(listDishesDomainValidator).validate(1L, null, 0, 10);
        when(dishRetrieveValidator.validateRestaurantExists(1L)).thenReturn(Mono.empty());
        when(dishPersistencePort.findActiveByRestaurantId(1L, 0, 10)).thenReturn(Flux.just(dish));
        when(dishPersistencePort.countActiveByRestaurantId(1L)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.listByRestaurant(1L, null, 0, 10))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals(1L, result.totalElements());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoDishes() {
        doNothing().when(listDishesDomainValidator).validate(1L, null, 0, 10);
        when(dishRetrieveValidator.validateRestaurantExists(1L)).thenReturn(Mono.empty());
        when(dishPersistencePort.findActiveByRestaurantId(1L, 0, 10))
                .thenReturn(Flux.empty());
        when(dishPersistencePort.countActiveByRestaurantId(1L))
                .thenReturn(Mono.just(0L));

        StepVerifier.create(service.listByRestaurant(1L, null, 0, 10))
                .assertNext(result -> {
                    Assertions.assertTrue(result.content().isEmpty());
                    Assertions.assertEquals(0L, result.totalElements());
                    Assertions.assertEquals(0, result.totalPages());
                    Assertions.assertEquals(0, result.page());
                    Assertions.assertEquals(10, result.size());
                })
                .verifyComplete();
    }

    @Test
    void shouldListDishesSuccessfullyWithCategory() {
        Dish dish = Dish.builder()
                .id(1L)
                .name("Burger")
                .price(BigDecimal.valueOf(20000))
                .restaurantId(1L)
                .build();

        doNothing().when(listDishesDomainValidator)
                .validate(1L, "Hamburguesas", 0, 10);

        when(dishRetrieveValidator.validateRestaurantExists(1L))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantIdAndCategory(
                1L, "Hamburguesas", 0, 10))
                .thenReturn(Flux.just(dish));

        when(dishPersistencePort.countActiveByRestaurantIdAndCategory(
                1L, "Hamburguesas"))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        service.listByRestaurant(1L, "Hamburguesas", 0, 10)
                )
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals(1L, result.totalElements());
                    Assertions.assertEquals(1, result.totalPages());
                })
                .verifyComplete();
    }

    @Test
    void shouldCalculateTotalPagesWhenFilteringByCategory() {
        Dish dish1 = Dish.builder()
                .id(1L)
                .name("Burger 1")
                .price(BigDecimal.valueOf(20000))
                .restaurantId(1L)
                .build();

        Dish dish2 = Dish.builder()
                .id(2L)
                .name("Burger 2")
                .price(BigDecimal.valueOf(25000))
                .restaurantId(1L)
                .build();

        doNothing().when(listDishesDomainValidator)
                .validate(1L, "Hamburguesas", 0, 2);

        when(dishRetrieveValidator.validateRestaurantExists(1L))
                .thenReturn(Mono.empty());

        when(dishPersistencePort.findActiveByRestaurantIdAndCategory(
                1L, "Hamburguesas", 0, 2))
                .thenReturn(Flux.just(dish1, dish2));

        when(dishPersistencePort.countActiveByRestaurantIdAndCategory(
                1L, "Hamburguesas"))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(
                        service.listByRestaurant(1L, "Hamburguesas", 0, 2)
                )
                .assertNext(result -> {
                    Assertions.assertEquals(2, result.content().size());
                    Assertions.assertEquals(5L, result.totalElements());
                    Assertions.assertEquals(3, result.totalPages());
                })
                .verifyComplete();
    }
}
