package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishRetrieveValidatorTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private DishRetrieveValidator dishRetrieveValidator;

    @Test
    void shouldValidateRestaurantExistsSuccessfully() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(true));

        StepVerifier.create(dishRetrieveValidator.validateRestaurantExists(1L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantNotFound() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(dishRetrieveValidator.validateRestaurantExists(1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }
}
