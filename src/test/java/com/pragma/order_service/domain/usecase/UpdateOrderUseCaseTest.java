package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;
    @Mock
    private IUserWebClientPort userWebClientPort;
    @Mock
    private INotificationWebClientPort notificationWebClientPort;
    @Mock
    private ITraceabilityWebClientPort traceabilityWebClientPort;
    @Mock
    private UpdateOrderDomainValidator updateOrderDomainValidator;
    @Mock
    private OrderStatusUpdateValidator orderStatusUpdateValidator;
    @Mock
    private OrderPinValidator orderPinValidator;

    @InjectMocks
    private UpdateOrderUseCase service;

    @Test
    void shouldAssignOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .customerId(20L)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .restaurantId(5L)
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(updateOrderDomainValidator).validate(100L, command);
        when(orderPersistencePort.findById(100L)).thenReturn(Mono.just(order));
        when(orderStatusUpdateValidator.validateEmployeeCanAssignOrder(30L, "EMPLEADO", order)).thenReturn(Mono.empty());
        when(orderStatusUpdateValidator.findRestaurantIdByEmployeeOrFail(30L)).thenReturn(Mono.just(5L));
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(orderDetail));
        when(traceabilityWebClientPort.create(any(), any())).thenReturn(Mono.just(
                TraceabilityRecord.builder().id("trace-1").orderId(100L).build()
        ));

        StepVerifier.create(service.update(100L, command, 30L, "EMPLEADO", "Martin Lopez", "12345678", "token-test"))
                .expectNext(100L)
                .verifyComplete();
    }

    @Test
    void shouldMarkOrderReadySuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .customerId(20L)
                .employeeAssignedId(30L)
                .status(OrderStatus.IN_PREPARATION)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.READY)
                .restaurantId(5L)
                .customerId(20L)
                .employeeAssignedId(30L)
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(20L)
                .phone("+51999999999")
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .restaurantId(5L)
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(updateOrderDomainValidator).validate(100L, command);
        when(orderPersistencePort.findById(100L)).thenReturn(Mono.just(order));
        when(orderStatusUpdateValidator.validateEmployeeCanMarkOrderReady(30L, "EMPLEADO", order)).thenReturn(Mono.empty());
        when(userWebClientPort.findById(20L, "token-test")).thenReturn(Mono.just(userSummary));
        when(notificationWebClientPort.sendReadyNotification("+51999999999", "token-test"))
                .thenReturn(Mono.just(com.pragma.order_service.domain.model.Notification.builder()
                        .phoneNumber("+51999999999")
                        .message("ok")
                        .build()));
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(orderDetail));
        when(traceabilityWebClientPort.create(any(), any())).thenReturn(Mono.just(
                TraceabilityRecord.builder().id("trace-1").orderId(100L).build()
        ));

        StepVerifier.create(service.update(100L, command, 30L, "EMPLEADO", "Martin Lopez", "12345678", "token-test"))
                .expectNext(100L)
                .verifyComplete();
    }

    @Test
    void shouldDeliverOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .customerId(20L)
                .employeeAssignedId(30L)
                .status(OrderStatus.READY)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.DELIVERED)
                .restaurantId(5L)
                .customerId(20L)
                .employeeAssignedId(30L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .restaurantId(5L)
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(updateOrderDomainValidator).validate(100L, command);
        when(orderPersistencePort.findById(100L)).thenReturn(Mono.just(order));
        when(orderStatusUpdateValidator.validateEmployeeCanDeliverOrder(30L, "EMPLEADO", order)).thenReturn(Mono.empty());
        when(orderPinValidator.validateDeliveryPin("12345678", "151370")).thenReturn(Mono.empty());
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(orderDetail));
        when(traceabilityWebClientPort.create(any(), any())).thenReturn(Mono.just(
                TraceabilityRecord.builder().id("trace-1").orderId(100L).build()
        ));

        StepVerifier.create(service.update(100L, command, 30L, "EMPLEADO", "Martin Lopez", "12345678", "token-test"))
                .expectNext(100L)
                .verifyComplete();
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.CANCELLED)
                .restaurantId(5L)
                .customerId(20L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .restaurantId(5L)
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(updateOrderDomainValidator).validate(100L, command);
        when(orderPersistencePort.findById(100L)).thenReturn(Mono.just(order));
        when(orderStatusUpdateValidator.validateClientCanCancelOrder(20L, "CLIENTE", order)).thenReturn(Mono.empty());
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(orderDetail));
        when(traceabilityWebClientPort.create(any(), any())).thenReturn(Mono.just(
                TraceabilityRecord.builder().id("trace-1").orderId(100L).build()
        ));

        StepVerifier.create(service.update(100L, command, 20L, "CLIENTE", "Juan Perez", null, "token-test"))
                .expectNext(100L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenOrderStatusUpdateIsNotSupported() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.PENDING, null);

        Order order = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .customerId(20L)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderDomainValidator)
                .validate(100L, command);

        when(orderPersistencePort.findById(100L))
                .thenReturn(Mono.just(order));

        StepVerifier.create(
                        service.update(
                                100L,
                                command,
                                30L,
                                "EMPLEADO",
                                "Martin Lopez",
                                "12345678",
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);

                    DomainException domainException = (DomainException) error;

                    Assertions.assertEquals(
                            DomainErrorCode.VALIDATION_ERROR,
                            domainException.getCode()
                    );

                    Assertions.assertEquals(
                            DomainErrorMessages.ORDER_STATUS_UPDATE_NOT_SUPPORTED,
                            domainException.getMessage()
                    );
                })
                .verify();
    }
}
