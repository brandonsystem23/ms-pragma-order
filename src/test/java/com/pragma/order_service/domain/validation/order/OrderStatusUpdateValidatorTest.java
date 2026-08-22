package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
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
class OrderStatusUpdateValidatorTest {

    @Mock
    private IRedisCachePort iRedisCachePort;

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @InjectMocks
    private OrderStatusUpdateValidator validator;

    @Test
    void shouldGetSessionSuccessfully() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .fullName("Martin Lopez")
                .numberDocument("12345678")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(session));

        StepVerifier.create(validator.getSession("token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(30L, result.userId());
                    Assertions.assertEquals("EMPLEADO", result.role());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailGetSessionWhenTokenIsInvalid() {
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(validator.getSession("bad-token"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateAssignOrderSuccessfully() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong()))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.validateAssignOrder(order, session))
                .verifyComplete();
    }

    @Test
    void shouldFailAssignWhenUserIsNotEmployee() {
        AuthSession session = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateAssignOrder(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("No tienes permisos para asignarte pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailAssignWhenEmployeeHasAnotherRestaurant() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong()))
                .thenReturn(Mono.just(99L));

        StepVerifier.create(validator.validateAssignOrder(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("No puedes asignarte pedidos de otro restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailAssignWhenStatusIsInvalid() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.READY)
                .employeeAssignedId(null)
                .build();

        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong()))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.validateAssignOrder(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("Solo se pueden asignar pedidos en estado PENDIENTE", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailAssignWhenOrderAlreadyAssigned() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(40L)
                .build();

        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong()))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.validateAssignOrder(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("El pedido ya tiene un empleado asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateMarkReadySuccessfully() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateMarkReady(order, session))
                .verifyComplete();
    }

    @Test
    void shouldFailMarkReadyWhenStatusIsInvalid() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateMarkReady(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("Solo se pueden marcar como listos pedidos en estado EN_PREPARACION", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailMarkReadyWhenAssignedEmployeeIsDifferent() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(99L)
                .build();

        StepVerifier.create(validator.validateMarkReady(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("No puedes marcar como listo un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateDeliverSuccessfully() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateDeliver(order, session))
                .verifyComplete();
    }

    @Test
    void shouldFailDeliverWhenStatusIsInvalid() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateDeliver(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("Solo se pueden marcar como entregados pedidos en estado LISTO", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailDeliverWhenAssignedEmployeeIsDifferent() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .status(OrderStatus.READY)
                .employeeAssignedId(99L)
                .build();

        StepVerifier.create(validator.validateDeliver(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("No puedes entregar un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateCancelSuccessfully() {
        AuthSession session = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateCancel(order, session))
                .verifyComplete();
    }

    @Test
    void shouldFailCancelWhenUserIsNotClient() {
        AuthSession session = AuthSession.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        Order order = Order.builder()
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateCancel(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("No tienes permisos para cancelar pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailCancelWhenOrderBelongsToAnotherClient() {
        AuthSession session = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .customerId(99L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateCancel(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("No puedes cancelar un pedido de otro cliente", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailCancelWhenStatusIsInvalid() {
        AuthSession session = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Order order = Order.builder()
                .customerId(20L)
                .status(OrderStatus.IN_PREPARATION)
                .build();

        StepVerifier.create(validator.validateCancel(order, session))
                .expectErrorSatisfies(error -> {
                    Assertions.assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", error.getMessage());
                })
                .verify();
    }
}
