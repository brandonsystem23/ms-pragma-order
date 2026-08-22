package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Notification;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
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
    private UpdateOrderDomainValidator updateOrderStatusDomainValidator;

    @InjectMocks
    private UpdateOrderUseCase service;

    private static final String TOKEN = "token-test";
    private static final Long ORDER_ID = 100L;
    private static final Long EMPLOYEE_ID = 30L;
    private static final Long CUSTOMER_ID = 20L;
    private static final Long RESTAURANT_ID = 5L;
    private static final String EMPLOYEE_DOCUMENT = "12345678";


    @Test
    void shouldAssignOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(RESTAURANT_ID));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldFailAssignOrderWhenUserIsNotEmployee() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession sessionRedis = AuthSession.builder()
                .userId(CUSTOMER_ID)
                .role("ROL_INVALIDO")
                .numberDocument("99999999")
                .phone("+573001112233")
                .email("test@test.com")
                .fullName("Usuario Prueba")
                .build();

        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(null)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong()))
                .thenReturn(Mono.just(RESTAURANT_ID));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    assertEquals(DomainException.class, error.getClass());
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ACCESS_DENIED, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenEmployeeHasNoRestaurant() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.EMPLOYEE_RESTAURANT_NOT_FOUND, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenRestaurantIsDifferent() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(999L));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ACCESS_DENIED, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenOrderStatusIsNotPending() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.READY)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(RESTAURANT_ID));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailAssignOrderWhenAlreadyAssigned() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(99L)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRestaurantPersistencePort.findRestaurantIdByEmployeeId(anyLong())).thenReturn(Mono.just(RESTAURANT_ID));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldMarkOrderReadySuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        AuthSession sessionRedis = employeeSessionRedis();
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

        UserSummary userSummary = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("+51900671048")
                .status(true)
                .roleName("CLIENTE")
                .build();

        Notification notification = Notification.builder()
                .phoneNumber("+51900671048")
                .message("Tu pedido está listo")
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(userSummary));
        when(iNotificationWebClientPort.sendReadyNotification(anyString(), anyString()))
                .thenReturn(Mono.just(notification));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldFailMarkOrderReadyWhenStatusIsNotInPreparation() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailMarkOrderReadyWhenEmployeeIsNotAssigned() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(999L)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ACCESS_DENIED, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailWhenNotificationFailsAndNotSaveOrder() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .restaurantId(RESTAURANT_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        UserSummary userSummary = UserSummary.builder()
                .id(CUSTOMER_ID)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("+51900671048")
                .status(true)
                .roleName("CLIENTE")
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iUserWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(userSummary));
        when(iNotificationWebClientPort.sendReadyNotification(anyString(), anyString()))
                .thenReturn(Mono.error(new RuntimeException("Error enviando notificación")));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void shouldDeliverOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        AuthSession sessionRedis = employeeSessionRedisWithDocument();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.READY)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.DELIVERED)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRedisCachePort.existsByEmployeeDocumentAndPin(anyString(), anyString()))
                .thenReturn(Mono.just(true));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldFailDeliverOrderWhenStatusIsNotReady() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        AuthSession sessionRedis = employeeSessionRedisWithDocument();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailDeliverOrderWhenEmployeeIsNotAssigned() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        AuthSession sessionRedis = employeeSessionRedisWithDocument();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.READY)
                .employeeAssignedId(999L)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ACCESS_DENIED, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailDeliverOrderWhenPinIsInvalid() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, "151370");

        AuthSession sessionRedis = employeeSessionRedisWithDocument();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.READY)
                .employeeAssignedId(EMPLOYEE_ID)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iRedisCachePort.existsByEmployeeDocumentAndPin(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.INVALID_PIN, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldCancelOrderSuccessfully() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession sessionRedis = clientSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = Order.builder()
                .id(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.CANCELLED)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));
        when(iOrderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .assertNext(result -> assertEquals(ORDER_ID, result))
                .verifyComplete();
    }

    @Test
    void shouldFailCancelOrderWhenUserIsNotClient() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ACCESS_DENIED, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailCancelOrderWhenCustomerIsDifferent() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession sessionRedis = clientSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .customerId(999L)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ACCESS_DENIED, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailCancelOrderWhenStatusIsNotPending() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        AuthSession sessionRedis = clientSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .customerId(CUSTOMER_ID)
                .status(OrderStatus.IN_PREPARATION)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.CANCELLED, null);

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.empty());

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

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(clientSessionRedis()));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.ORDER_NOT_FOUND, ex.getCode());
                })
                .verify();
    }

    @Test
    void shouldFailWhenStatusIsUnsupported() {
        UpdateOrderCommand command = new UpdateOrderCommand("PENDIENTE", null);

        AuthSession sessionRedis = employeeSessionRedis();
        Order order = Order.builder()
                .id(ORDER_ID)
                .status(OrderStatus.PENDING)
                .build();

        doNothing().when(updateOrderStatusDomainValidator).validate(anyLong(), any());
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(sessionRedis));
        when(iOrderPersistencePort.findById(anyLong())).thenReturn(Mono.just(order));

        StepVerifier.create(service.update(ORDER_ID, command, TOKEN))
                .expectErrorSatisfies(error -> {
                    DomainException ex = (DomainException) error;
                    assertEquals(DomainErrorCode.VALIDATION_ERROR, ex.getCode());
                })
                .verify();
    }

    private AuthSession employeeSessionRedis() {
        return AuthSession.builder()
                .userId(EMPLOYEE_ID)
                .role("EMPLEADO")
                .build();
    }

    private AuthSession employeeSessionRedisWithDocument() {
        return AuthSession.builder()
                .userId(EMPLOYEE_ID)
                .role("EMPLEADO")
                .numberDocument(EMPLOYEE_DOCUMENT)
                .build();
    }

    private AuthSession clientSessionRedis() {
        return AuthSession.builder()
                .userId(CUSTOMER_ID)
                .role("CLIENTE")
                .build();
    }
}
