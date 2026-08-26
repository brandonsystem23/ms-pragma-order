package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
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
class DishRegistrationValidatorTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @InjectMocks
    private DishRegistrationValidator dishRegistrationValidator;

    @Test
    void shouldValidateDishCreationRulesSuccessfully() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(true));
        when(restaurantPersistencePort.existByOwner(1L, 2L)).thenReturn(Mono.just(true));
        when(dishPersistencePort.existsByName("Pizza Hawaiana")).thenReturn(Mono.just(false));

        StepVerifier.create(dishRegistrationValidator.validateDishCreationRules("Pizza Hawaiana", 1L, 2L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(dishRegistrationValidator.validateDishCreationRules("Pizza Hawaiana", 1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenUserIsNotOwner() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(true));
        when(restaurantPersistencePort.existByOwner(1L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(dishRegistrationValidator.validateDishCreationRules("Pizza Hawaiana", 1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Usted no es propietario del restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishNameAlreadyExists() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(true));
        when(restaurantPersistencePort.existByOwner(1L, 2L)).thenReturn(Mono.just(true));
        when(dishPersistencePort.existsByName("Pizza Hawaiana")).thenReturn(Mono.just(true));

        StepVerifier.create(dishRegistrationValidator.validateDishCreationRules("Pizza Hawaiana", 1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El nombre del palto ya está registrado", error.getMessage());
                })
                .verify();
    }
}
