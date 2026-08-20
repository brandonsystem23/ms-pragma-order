package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
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
class MarkOrderReadyValidatorTest {

    @Mock
    private AuthSessionPort authSessionPort;

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @InjectMocks
    private MarkOrderReadyValidator validator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(OrderStatus.READY, result.getStatus());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
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
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("CLIENTE")
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para marcar pedidos como listos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderDoesNotExist() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
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
    void shouldFailWhenOrderStatusIsNotInPreparation() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(30L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Solo se pueden marcar como listos pedidos en estado EN_PREPARACION", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenEmployeeIsNotAssignedToOrder() {
        AuthSession authSession = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .id(100L)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(44L)
                .build();

        when(authSessionPort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(orderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(validator.validate(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes marcar como listo un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }
}
