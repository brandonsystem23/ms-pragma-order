package com.pragma.order_service.domain.validation.restaurant;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantRegistrationValidatorTest {

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @Mock
    private IRedisCachePort iRedisCachePort;

    @Mock
    private IUserWebClientPort iUserWebClientPort;

    @InjectMocks
    private RestaurantRegistrationValidator restaurantRegistrationValidator;


    @Test
    void shouldValidateSuccessfully() {
        AuthSession authSession = AuthSession.builder()
                .userId(1L)
                .role("ADMINISTRADOR")
                .build();

        UserSummary owner = UserSummary.builder()
                .id(2L)
                .status(true)
                .roleName("PROPIETARIO")
                .build();


        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(owner));

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "token-test"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.empty());
        when(iRestaurantPersistencePort.existsByNit(any())).thenReturn(Mono.just(true));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "bad-token"))
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

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existsByNit(any())).thenReturn(Mono.just(true));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "token-test"))
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

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(true));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "token-test"))
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

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.empty());

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "token-test"))
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

        UserSummary owner = UserSummary.builder()
                .id(2L)
                .status(false)
                .roleName("PROPIETARIO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(owner));

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "token-test"))
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

        UserSummary owner = UserSummary.builder()
                .id(2L)
                .status(true)
                .roleName("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iRestaurantPersistencePort.existsByNit(anyString())).thenReturn(Mono.just(false));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(owner));

        StepVerifier.create(restaurantRegistrationValidator.validate("123456789", 2L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El usuario indicado no tiene rol PROPIETARIO", error.getMessage());
                })
                .verify();
    }
}
