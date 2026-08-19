package com.pragma.order_service.domain.service.restaurant;

import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.domain.service.restaurant.validation.ListRestaurantsDomainValidator;
import com.pragma.order_service.domain.service.restaurant.validation.RestaurantRetrieveValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRestaurantsServiceTest {

    @Mock
    private RestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private RestaurantRetrieveValidator restaurantRetrieveValidator;

    @Mock
    private ListRestaurantsDomainValidator listRestaurantsDomainValidator;

    @InjectMocks
    private ListRestaurantsService service;

    @Test
    void shouldListRestaurantsSuccessfully() {
        Restaurant restaurant1 = Restaurant.builder()
                .id(1L)
                .name("Burger House")
                .urlLogo("https://logo.com/burger.png")
                .status(true)
                .build();

        Restaurant restaurant2 = Restaurant.builder()
                .id(2L)
                .name("Pizza Place")
                .urlLogo("https://logo.com/pizza.png")
                .status(true)
                .build();

        when(restaurantRetrieveValidator.validate(anyString())).thenReturn(Mono.empty());
        doNothing().when(listRestaurantsDomainValidator).validate(anyInt(), anyInt());
        when(restaurantPersistencePort.findActiveRestaurantsOrdered(anyInt(), anyInt()))
                .thenReturn(Flux.just(restaurant1, restaurant2));
        when(restaurantPersistencePort.countActiveRestaurants())
                .thenReturn(Mono.just(2L));


        StepVerifier.create(service.list("token-test", 0, 10))
                .assertNext(response -> {
                    Assertions.assertEquals(2, response.content().size());
                    Assertions.assertEquals(0, response.page());
                    Assertions.assertEquals(10, response.size());
                    Assertions.assertEquals(2L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());

                    RestaurantListItemResponse first = response.content().getFirst();
                    Assertions.assertEquals("Burger House", first.name());
                    Assertions.assertEquals("https://logo.com/burger.png", first.urlLogo());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenNoRestaurantsExist() {
        doNothing().when(listRestaurantsDomainValidator).validate(anyInt(), anyInt());
        when(restaurantRetrieveValidator.validate(anyString())).thenReturn(Mono.empty());
        when(restaurantPersistencePort.findActiveRestaurantsOrdered(anyInt(), anyInt()))
                .thenReturn(Flux.empty());
        when(restaurantPersistencePort.countActiveRestaurants())
                .thenReturn(Mono.just(0L));

        StepVerifier.create(service.list("token-test", 0, 10))
                .assertNext(response -> {
                    Assertions.assertTrue(response.content().isEmpty());
                    Assertions.assertEquals(0L, response.totalElements());
                    Assertions.assertEquals(0, response.totalPages());
                })
                .verifyComplete();
    }
}
