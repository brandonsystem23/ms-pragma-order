package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DishRegistrationValidatorTest {

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private IRedisCachePort iRedisCachePort;

    @InjectMocks
    private DishRegistrationValidator dishRegistrationValidator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSession authSession = AuthSession.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(iRestaurantPersistencePort.existByOwner(anyLong(), anyLong())).thenReturn(Mono.just(true));
        when(dishPersistencePort.existsByName(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(dishRegistrationValidator.validate("Pizza Hawaiana", 1L, "token-test"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.empty());

        when(dishPersistencePort.existsByName(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(dishRegistrationValidator.validate("Pizza Hawaiana", 1L, "bad-token"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenAuthenticatedUserIsNotOwner() {
        AuthSession authSession = AuthSession.builder()
                .userId(2L)
                .role("ADMINISTRADOR")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(dishPersistencePort.existsByName(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(dishRegistrationValidator.validate("Pizza Hawaiana", 1L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para crear platos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {
        AuthSession authSession = AuthSession.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(false));
        when(dishPersistencePort.existsByName(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(dishRegistrationValidator.validate("Pizza Hawaiana", 1L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenUserIsNotOwnerOfRestaurant() {
        AuthSession authSession = AuthSession.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(dishPersistencePort.existsByName(anyString())).thenReturn(Mono.just(true));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(iRestaurantPersistencePort.existByOwner(anyLong(), anyLong())).thenReturn(Mono.just(false));

        StepVerifier.create(dishRegistrationValidator.validate("Pizza Hawaiana", 1L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Usted no es propietario del restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishNameAlreadyExists() {
        AuthSession authSession = AuthSession.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(iRestaurantPersistencePort.existByOwner(anyLong(), anyLong())).thenReturn(Mono.just(true));
        when(dishPersistencePort.existsByName(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(dishRegistrationValidator.validate("Pizza Hawaiana", 1L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El nombre del palto ya está registrado", error.getMessage());
                })
                .verify();
    }
}
