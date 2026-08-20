package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.port.out.OrderPinValidationPort;
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
class DeliverOrderValidatorTest {

    @Mock
    private AuthSessionPort authSessionPort;

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private OrderPinValidationPort orderPinValidationPort;

    @InjectMocks
    private DeliverOrderValidator validator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(orderPinValidationPort.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .thenReturn(Mono.just(true));

        StepVerifier.create(validator.validate(100L, "151370", "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(OrderStatus.DELIVERED, result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(100L, "151370", "bad-token"))
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
                .numberDocument("12345678")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate(100L, "151370", "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para entregar pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderDoesNotExist() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(validator.validate(100L, "151370", "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El pedido no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderStatusIsNotReady() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "151370", "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Solo se pueden marcar como entregados pedidos en estado LISTO", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenEmployeeIsNotAssignedToOrder() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.READY)
                .employeeAssignedId(44L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "151370", "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes entregar un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenPinIsInvalid() {
        AuthSessionRedisValue authSession = AuthSessionRedisValue.builder()
                .userId(30L)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .build();

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(orderPinValidationPort.existsByEmployeeDocumentAndPin("12345678", "151370"))
                .thenReturn(Mono.just(false));

        StepVerifier.create(validator.validate(100L, "151370", "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El PIN de seguridad es inválido", error.getMessage());
                })
                .verify();
    }
}
