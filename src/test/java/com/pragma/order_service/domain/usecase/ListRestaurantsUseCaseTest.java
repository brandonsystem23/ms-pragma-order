package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.validation.restaurant.ListRestaurantsDomainValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRestaurantsUseCaseTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private ListRestaurantsDomainValidator listRestaurantsDomainValidator;

    @InjectMocks
    private ListRestaurantsUseCase service;

    @Test
    void shouldListRestaurantsSuccessfully() {
        Restaurant restaurant1 = Restaurant.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://logo.com/burger.png")
                .status(true)
                .build();

        doNothing().when(listRestaurantsDomainValidator).validate(0, 10);
        when(restaurantPersistencePort.findActiveRestaurantsOrdered(0, 10)).thenReturn(Flux.just(restaurant1));
        when(restaurantPersistencePort.countActiveRestaurants()).thenReturn(Mono.just(1L));

        StepVerifier.create(service.list(0, 10))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals(1L, result.totalElements());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenThereAreNoRestaurants() {
        doNothing().when(listRestaurantsDomainValidator)
                .validate(0, 10);

        when(restaurantPersistencePort.findActiveRestaurantsOrdered(0, 10))
                .thenReturn(Flux.empty());

        when(restaurantPersistencePort.countActiveRestaurants())
                .thenReturn(Mono.just(0L));

        StepVerifier.create(service.list(0, 10))
                .assertNext(result -> {
                    Assertions.assertTrue(result.content().isEmpty());
                    Assertions.assertEquals(0L, result.totalElements());
                    Assertions.assertEquals(0, result.totalPages());
                    Assertions.assertEquals(0, result.page());
                    Assertions.assertEquals(10, result.size());
                })
                .verifyComplete();
    }
}
