package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignOrderValidatorTest {

    @Mock
    private AuthSessionPort authSessionPort;

    @Mock
    private RestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @InjectMocks
    private AssignOrderValidator validator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(5L));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
                    Assertions.assertEquals(OrderStatus.IN_PREPARATION, result.getStatus());
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
    void shouldFailWhenRoleIsNotEmployee() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para asignarte pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenEmployeeHasNoRestaurantAssigned() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El empleado no tiene un restaurante asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderDoesNotExist() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(5L));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El pedido no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderBelongsToAnotherRestaurant() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .restaurantId(99L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(5L));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes asignarte pedidos de otro restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderStatusIsNotPending() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.READY)
                .employeeAssignedId(null)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(5L));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Solo se pueden asignar pedidos en estado PENDIENTE", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderAlreadyHasEmployeeAssigned() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(44L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(5L));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El pedido ya tiene un empleado asignado", error.getMessage());
                })
                .verify();
    }
}
