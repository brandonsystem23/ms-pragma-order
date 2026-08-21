package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRetrieveValidatorTest {

    @Mock
    private IRedisCachePort iRedisCachePort;

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @InjectMocks
    private OrderRetrieveValidator orderRetrieveValidator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(5L));

        StepVerifier.create(orderRetrieveValidator.validate("token-test"))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(orderRetrieveValidator.validate("bad-token"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRoleIsNotEmployee() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(orderRetrieveValidator.validate("token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para listar pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenEmployeeHasNoRestaurantAssigned() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(orderRetrieveValidator.validate("token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El empleado no tiene un restaurante asignado", error.getMessage());
                })
                .verify();
    }
}
