package com.pragma.order_service.domain.validation.restaurant;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
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
class RestaurantRegistrationValidatorTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IUserWebClientPort userWebClientPort;

    @InjectMocks
    private RestaurantRegistrationValidator restaurantRegistrationValidator;

    @Test
    void shouldValidateRestaurantCreationRulesSuccessfully() {
        UserSummary owner = UserSummary.builder()
                .id(2L)
                .status(true)
                .roleName("PROPIETARIO")
                .build();

        when(restaurantPersistencePort.existsByNit("123456789")).thenReturn(Mono.just(false));
        when(userWebClientPort.findById(2L, "token-test")).thenReturn(Mono.just(owner));

        StepVerifier.create(restaurantRegistrationValidator.validateRestaurantCreationRules("123456789", 2L, "token-test"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenNitAlreadyExists() {
        when(restaurantPersistencePort.existsByNit("123456789")).thenReturn(Mono.just(true));

        StepVerifier.create(restaurantRegistrationValidator.validateRestaurantCreationRules("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El NIT ya está registrado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOwnerIsInactive() {
        UserSummary owner = UserSummary.builder()
                .id(2L)
                .status(false)
                .roleName("PROPIETARIO")
                .build();

        StepVerifier.create(restaurantRegistrationValidator.validateOwnerIsActive(owner))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El propietario no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOwnerDoesNotHaveOwnerRole() {
        UserSummary owner = UserSummary.builder()
                .id(2L)
                .status(true)
                .roleName("EMPLEADO")
                .build();

        StepVerifier.create(restaurantRegistrationValidator.validateOwnerHasOwnerRole(owner))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El usuario indicado no tiene rol PROPIETARIO", error.getMessage());
                })
                .verify();
    }
}
