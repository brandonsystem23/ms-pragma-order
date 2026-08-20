package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRegistrationValidatorTest {

    @Mock
    private AuthSessionPort authSessionPort;

    @Mock
    private RestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private DishPersistencePort dishPersistencePort;

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @InjectMocks
    private OrderRegistrationValidator validator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Dish dish1 = Dish.builder()
                .id(10L)
                .restaurantId(1L)
                .name("Pizza")
                .price(BigDecimal.valueOf(20000))
                .status(true)
                .build();

        Dish dish2 = Dish.builder()
                .id(11L)
                .restaurantId(1L)
                .name("Hamburguesa")
                .price(BigDecimal.valueOf(15000))
                .status(true)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(restaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(dishPersistencePort.findByIdAndStatusTrue(anyLong()))
                .thenReturn(Mono.just(dish1))
                .thenReturn(Mono.just(dish2));

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(
                                new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                                new CreateOrderItemCommand(11L, BigDecimal.ONE)
                        ),
                        "token-test"
                ))
                .expectNext(20L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "bad-token"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRoleIsNotClient() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("PROPIETARIO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para crear pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenCustomerHasActiveOrder() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(true));

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El cliente ya tiene un pedido en proceso para este restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(restaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(false));

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishDoesNotExist() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(restaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(dishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishBelongsToAnotherRestaurant() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(99L)
                .name("Pizza")
                .price(BigDecimal.valueOf(20000))
                .status(true)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(restaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(dishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.just(dish));

        StepVerifier.create(validator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Todos los platos del pedido deben pertenecer al restaurante indicado", error.getMessage());
                })
                .verify();
    }
}
