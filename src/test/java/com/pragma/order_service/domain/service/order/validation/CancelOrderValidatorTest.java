package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.infrastructure.output.redis.dto.AuthSessionRedisValue;
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
class CancelOrderValidatorTest {

    @Mock
    private AuthSessionPort authSessionPort;

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @InjectMocks
    private CancelOrderValidator validator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .id(100L)
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(OrderStatus.CANCELLED, result.getStatus());
                    Assertions.assertEquals(20L, result.getCustomerId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(100L, "bad-token"))
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
                .role("EMPLEADO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para cancelar pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderDoesNotExist() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El pedido no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderBelongsToAnotherCustomer() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .id(100L)
                .customerId(99L)
                .status(OrderStatus.PENDING)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes cancelar un pedido de otro cliente", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderStatusIsNotPending() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .id(100L)
                .customerId(20L)
                .status(OrderStatus.IN_PREPARATION)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", error.getMessage());
                })
                .verify();
    }
}
