package com.pragma.order_service.domain.service.restaurant.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantRetrieveValidatorTest {

    @Mock
    private AuthSessionPort authSessionPort;

    @InjectMocks
    private RestaurantRetrieveValidator validator;

    @Test
    void shouldValidateSuccessfullyWhenRoleIsClient() {
        AuthSession authSession = AuthSession.builder()
                .userId(10L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate("token-test"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate("bad-token"))
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

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate("token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para listar restaurantes", error.getMessage());
                })
                .verify();
    }
}
