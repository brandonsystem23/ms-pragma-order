package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Notification;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderPinValidator;
import com.pragma.order_service.domain.validation.order.OrderStatusUpdateValidator;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderUseCaseTest {

    @Mock
    private IOrderPersistencePort iOrderPersistencePort;

    @Mock
    private IRedisCachePort iRedisCachePort;

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @Mock
    private IUserWebClientPort iUserWebClientPort;

    @Mock
    private INotificationWebClientPort iNotificationWebClientPort;

    @Mock
    private ITraceabilityWebClientPort iTraceabilityWebClientPort;

    @Mock
    private UpdateOrderDomainValidator updateOrderStatusDomainValidator;

    @Mock
    private OrderStatusUpdateValidator orderStatusUpdateValidator;

    @Mock
    private OrderPinValidator orderPinValidator;

    @InjectMocks
    private UpdateOrderUseCase service;

    private static final String TOKEN = "token-test";
    private static final Long ORDER_ID = 100L;
    private static final Long EMPLOYEE_ID = 30L;
    private static final Long CUSTOMER_ID = 20L;
    private static final Long RESTAURANT_ID = 5L;

    @Test
    void shouldAssignOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession session = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        UserSummary customer = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(RESTAURANT_ID)
                .name("El Buen Sabor")
                .ownerId(99L)
                .build();

        TraceabilityRecord traceability = TraceabilityRecord.builder()
                .id("trace-1")
                .orderId(ORDER_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(orderStatusUpdateValidator.validateAssignOrder(any(), any())).thenReturn(Mono.empty());
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(iUserWebClientPort.findById(eq(CUSTOMER_ID), anyString())).thenReturn(Mono.just(customer));
        when(iRestaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Mono.just(restaurant));
        when(iTraceabilityWebClientPort.create(any(), anyString())).thenReturn(Mono.just(traceability));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldMarkOrderReadySuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        AuthSession session = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.READY)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        UserSummary customer = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .phone("+51900671048")
                .build();

        Notification notification = Notification.builder()
                .phoneNumber("+51900671048")
                .message("Tu pedido está listo")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(RESTAURANT_ID)
                .name("El Buen Sabor")
                .ownerId(99L)
                .build();

        TraceabilityRecord traceability = TraceabilityRecord.builder()
                .id("trace-2")
                .orderId(ORDER_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(orderStatusUpdateValidator.validateMarkReady(any(), any())).thenReturn(Mono.empty());
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iUserWebClientPort.findById(eq(CUSTOMER_ID), anyString())).thenReturn(Mono.just(customer));
        when(iNotificationWebClientPort.sendReadyNotification(anyString(), anyString()))
                .thenReturn(Mono.just(notification));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(iRestaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Mono.just(restaurant));
        when(iTraceabilityWebClientPort.create(any(), anyString())).thenReturn(Mono.just(traceability));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldDeliverOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        AuthSession session = employeeSessionRedisWithDocument();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.READY)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.DELIVERED)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        UserSummary customer = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(RESTAURANT_ID)
                .name("El Buen Sabor")
                .ownerId(99L)
                .build();

        TraceabilityRecord traceability = TraceabilityRecord.builder()
                .id("trace-3")
                .orderId(ORDER_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(orderStatusUpdateValidator.validateDeliver(any(), any())).thenReturn(Mono.empty());
        when(orderPinValidator.validateDeliveryPin(any(), anyString())).thenReturn(Mono.empty());
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(iUserWebClientPort.findById(eq(CUSTOMER_ID), anyString())).thenReturn(Mono.just(customer));
        when(iRestaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Mono.just(restaurant));
        when(iTraceabilityWebClientPort.create(any(), anyString())).thenReturn(Mono.just(traceability));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldFailDeliverWhenPinIsInvalid() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        AuthSession session = employeeSessionRedisWithDocument();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.READY)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(orderStatusUpdateValidator.validateDeliver(any(), any())).thenReturn(Mono.empty());
        when(orderPinValidator.validateDeliveryPin(any(), anyString()))
                .thenReturn(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_PIN,
                        "El PIN de seguridad es inválido"
                )));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.INVALID_PIN, ex.getCode());
                    assertEquals("El PIN de seguridad es inválido", ex.getMessage());
                })
                .verify();
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession session = clientSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.CANCELLED)
                .build();

        UserSummary customer = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(RESTAURANT_ID)
                .name("El Buen Sabor")
                .ownerId(99L)
                .build();

        TraceabilityRecord traceability = TraceabilityRecord.builder()
                .id("trace-4")
                .orderId(ORDER_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(orderStatusUpdateValidator.validateCancel(any(), any())).thenReturn(Mono.empty());
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(iUserWebClientPort.findById(eq(CUSTOMER_ID), anyString())).thenReturn(Mono.just(customer));
        when(iRestaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Mono.just(restaurant));
        when(iTraceabilityWebClientPort.create(any(), anyString())).thenReturn(Mono.just(traceability));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.error(
                new DomainException(DomainErrorCode.INVALID_TOKEN, "Token inválido o expirado")
        ));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.INVALID_TOKEN, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailWhenOrderNotFound() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession session = clientSessionRedis();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ORDER_NOT_FOUND, ex.getCode());
                    assertEquals("El pedido no existe", ex.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRestaurantNotFoundDuringTraceabilityCreation() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession session = clientSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.CANCELLED)
                .build();

        UserSummary customer = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(orderStatusUpdateValidator.validateCancel(any(), any())).thenReturn(Mono.empty());
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(iUserWebClientPort.findById(eq(CUSTOMER_ID), anyString())).thenReturn(Mono.just(customer));
        when(iRestaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Mono.empty());

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.RESTAURANT_NOT_FOUND, ex.getCode());
                    assertEquals("El restaurante no existe", ex.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenStatusIsNotSupportedInProcessStatusUpdate() {
        UpdateOrderCommand command = new UpdateOrderCommand("OTRO_ESTADO", null);

        AuthSession session = clientSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(orderStatusUpdateValidator.getSession(anyString())).thenReturn(Mono.just(session));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);

                    DomainException ex = (DomainException) error;
                    Assertions.assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                    Assertions.assertEquals(
                            "El estado solicitado no es soportado para actualización",
                            ex.getMessage()
                    );
                })
                .verify();
    }

    private AuthSession employeeSessionRedis() {
        return AuthSession.builder()
                .userId(EMPLOYEE_ID)
                .role("EMPLEADO")
                .fullName("Martin Lopez")
                .build();
    }

    private AuthSession employeeSessionRedisWithDocument() {
        return AuthSession.builder()
                .userId(EMPLOYEE_ID)
                .role("EMPLEADO")
                .numberDocument("12345678")
                .fullName("Martin Lopez")
                .build();
    }

    private AuthSession clientSessionRedis() {
        return AuthSession.builder()
                .userId(CUSTOMER_ID)
                .role("CLIENTE")
                .fullName("Juan Perez")
                .build();
    }
}
