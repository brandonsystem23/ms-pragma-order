package com.pragma.order_service.domain.service.dish.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.infrastructure.output.redis.dto.AuthSessionRedisValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDishRegistrationValidatorTest {

    @Mock
    private DishPersistencePort dishPersistencePort;

    @Mock
    private RestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private AuthSessionPort authSessionPort;

    @InjectMocks
    private UpdateDishRegistrationValidator validator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza")
                .price(BigDecimal.valueOf(25000))
                .description("Original")
                .restaurantId(10L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(dishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(validator.validate(1L, "token-test", null))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals(10L, result.getRestaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldValidateStatusSuccessfully() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        Dish dish = Dish.builder()
                .id(1L)
                .name("Pizza")
                .price(BigDecimal.valueOf(25000))
                .description("Original")
                .restaurantId(10L)
                .status(false)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(dishPersistencePort.findById(anyLong())).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(validator.validate(1L, "token-test", false))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals(10L, result.getRestaurantId());
                    Assertions.assertEquals(false, result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(1L, "bad-token", null))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenAuthenticatedUserIsNotOwner() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(2L)
                .role("ADMINISTRADOR")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate(1L, "token-test", null))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para crear platos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishDoesNotExist() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(dishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(1L, "token-test", null))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenUserIsNotOwnerOfRestaurant() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(2L)
                .role("PROPIETARIO")
                .build();

        Dish dish = Dish.builder()
                .id(1L)
                .restaurantId(10L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(dishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(validator.validate(1L, "token-test", null))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Usted no es propietario del restaurante", error.getMessage());
                })
                .verify();
    }
}
