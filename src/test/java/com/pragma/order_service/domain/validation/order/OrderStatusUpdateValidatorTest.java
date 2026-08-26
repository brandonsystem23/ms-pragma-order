package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusUpdateValidatorTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private OrderStatusUpdateValidator validator;

    @Test
    void shouldValidateAssignOrderSuccessfully() {
        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.validateEmployeeCanAssignOrder(30L, "EMPLEADO", order))
                .verifyComplete();
    }

    @Test
    void shouldFailAssignOrderWhenRoleIsNotEmployee() {
        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        StepVerifier.create(validator.validateEmployeeCanAssignOrder(30L, "CLIENTE", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para asignarte pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenEmployeeBelongsToDifferentRestaurant() {
        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.just(9L));

        StepVerifier.create(validator.validateEmployeeCanAssignOrder(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes asignarte pedidos de otro restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenOrderStatusIsNotPending() {
        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(null)
                .build();

        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.validateEmployeeCanAssignOrder(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Solo se pueden asignar pedidos en estado PENDIENTE", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenOrderAlreadyAssigned() {
        Order order = Order.builder()
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(99L)
                .build();

        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.validateEmployeeCanAssignOrder(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El pedido ya tiene un empleado asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateMarkReadySuccessfully() {
        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanMarkOrderReady(30L, "EMPLEADO", order))
                .verifyComplete();
    }

    @Test
    void shouldFailMarkReadyWhenRoleIsNotEmployee() {
        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanMarkOrderReady(30L, "CLIENTE", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para marcar pedidos como listos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailMarkReadyWhenStatusIsInvalid() {
        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanMarkOrderReady(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Solo se pueden marcar como listos pedidos en estado EN_PREPARACION", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailMarkReadyWhenEmployeeIsNotAssigned() {
        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(99L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanMarkOrderReady(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes marcar como listo un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailMarkReadyWhenOrderHasNoAssignedEmployee() {
        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(null)
                .build();

        StepVerifier.create(validator.validateEmployeeCanMarkOrderReady(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes marcar como listo un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateDeliverSuccessfully() {
        Order order = Order.builder()
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanDeliverOrder(30L, "EMPLEADO", order))
                .verifyComplete();
    }

    @Test
    void shouldFailDeliverWhenRoleIsNotEmployee() {
        Order order = Order.builder()
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanDeliverOrder(30L, "CLIENTE", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para entregar pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailDeliverWhenStatusIsInvalid() {
        Order order = Order.builder()
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanDeliverOrder(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Solo se pueden marcar como entregados pedidos en estado LISTO", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailDeliverWhenEmployeeIsNotAssigned() {
        Order order = Order.builder()
                .status(OrderStatus.READY)
                .employeeAssignedId(99L)
                .build();

        StepVerifier.create(validator.validateEmployeeCanDeliverOrder(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes entregar un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailDeliverWhenOrderHasNoAssignedEmployee() {
        Order order = Order.builder()
                .status(OrderStatus.READY)
                .employeeAssignedId(null)
                .build();

        StepVerifier.create(validator.validateEmployeeCanDeliverOrder(30L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes entregar un pedido que no tienes asignado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateCancelSuccessfully() {
        Order order = Order.builder()
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateClientCanCancelOrder(20L, "CLIENTE", order))
                .verifyComplete();
    }

    @Test
    void shouldFailCancelWhenRoleIsNotClient() {
        Order order = Order.builder()
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateClientCanCancelOrder(20L, "EMPLEADO", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para cancelar pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailCancelWhenCustomerDidNotCreateOrder() {
        Order order = Order.builder()
                .customerId(99L)
                .status(OrderStatus.PENDING)
                .build();

        StepVerifier.create(validator.validateClientCanCancelOrder(20L, "CLIENTE", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No puedes cancelar un pedido de otro cliente", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailCancelWhenStatusIsInvalid() {
        Order order = Order.builder()
                .customerId(20L)
                .status(OrderStatus.IN_PREPARATION)
                .build();

        StepVerifier.create(validator.validateClientCanCancelOrder(20L, "CLIENTE", order))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFindRestaurantIdByEmployeeSuccessfully() {
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(validator.findRestaurantIdByEmployeeOrFail(30L))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenEmployeeHasNoAssignedRestaurant() {
        when(restaurantPersistencePort.findRestaurantIdByEmployeeId(30L))
                .thenReturn(Mono.empty());

        StepVerifier.create(validator.findRestaurantIdByEmployeeOrFail(30L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El empleado no tiene un restaurante asignado", error.getMessage());
                })
                .verify();
    }
}
