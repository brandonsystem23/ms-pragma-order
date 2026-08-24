package com.pragma.order_service.domain.validation.dish;

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
class DishRetrieveValidatorTest {

    @Mock
    private IRedisCachePort iRedisCachePort;

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @InjectMocks
    private DishRetrieveValidator dishRetrieveValidator;

    @Test
    void shouldValidateSuccessfullyWhenRoleIsClient() {
        AuthSession authSession = AuthSession.builder()
                .userId(10L)
                .role("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));

        StepVerifier.create(dishRetrieveValidator.validate("token-test", 1L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {

        when(iRedisCachePort.findByToken(anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(dishRetrieveValidator.validate("bad-token", 1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRoleIsNotClient() {

        AuthSession authSession = AuthSession.builder()
                .userId(10L)
                .role("ADMINISTRADOR")
                .build();

        when(iRedisCachePort.findByToken(anyString()))
                .thenReturn(Mono.just(authSession));

        StepVerifier.create(dishRetrieveValidator.validate("token-test", 1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para listar platos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRestaurantNotFound() {
        AuthSession authSession = AuthSession.builder()
                .userId(10L)
                .role("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(false));

        StepVerifier.create(dishRetrieveValidator.validate("token-test", 1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }
}
