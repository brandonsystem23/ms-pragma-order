package com.pragma.order_service.domain.service.restaurant.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.domain.port.out.UserWebClientPort;
import com.pragma.order_service.infrastructure.output.webclient.dto.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantRegistrationValidatorTest {

    @Mock
    private RestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private AuthSessionPort authSessionPort;

    @Mock
    private UserWebClientPort userWebClientPort;

    @InjectMocks
    private RestaurantRegistrationValidator validator;


    @Test
    void shouldValidateSuccessfully() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        UserResponse owner = UserResponse.builder()
                .id(2L)
                .status(true)
                .roleName("PROPIETARIO")
                .build();


        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(owner));

        StepVerifier.create(validator.validate("123456789", 2L, "token-test"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.empty());
        when(restaurantPersistencePort.existsByNit(any())).thenReturn(Mono.just(true));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate("123456789", 2L, "bad-token"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenAuthenticatedUserIsNotAdmin() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("PROPIETARIO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.existsByNit(any())).thenReturn(Mono.just(true));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para crear restaurantes", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenNitAlreadyExists() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(true));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El NIT ya está registrado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOwnerDoesNotExist() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El propietario no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOwnerIsInactive() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        UserResponse owner = UserResponse.builder()
                .id(2L)
                .status(false)
                .roleName("PROPIETARIO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(owner));

        StepVerifier.create(validator.validate("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El propietario no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOwnerRoleIsInvalid() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        UserResponse owner = UserResponse.builder()
                .id(2L)
                .status(true)
                .roleName("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(owner));

        StepVerifier.create(validator.validate("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El usuario indicado no tiene rol PROPIETARIO", error.getMessage());
                })
                .verify();
    }
}
