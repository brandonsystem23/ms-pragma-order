package com.pragma.order_service.domain.validation.order;

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
class OrderRetrieveValidatorTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private OrderRetrieveValidator orderRetrieveValidator;

    @Test
    void shouldValidateEmployeeHasRestaurantAssigned() {
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(orderRetrieveValidator.validateEmployeeHasRestaurantAssigned(30L))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenEmployeeHasNoRestaurantAssigned() {
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderRetrieveValidator.validateEmployeeHasRestaurantAssigned(30L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El empleado no tiene un restaurante asignado", error.getMessage());
                })
                .verify();
    }
}
