package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.port.out.NotificationWebClientPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.port.out.UserWebClientPort;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.MarkOrderReadyValidator;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import com.pragma.order_service.infrastructure.output.webclient.dto.NotificationResponse;
import com.pragma.order_service.infrastructure.output.webclient.dto.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkOrderReadyServiceTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private MarkOrderReadyValidator markOrderReadyValidator;

    @Mock
    private AssignOrderDomainValidator assignOrderDomainValidator;

    @Mock
    private UserWebClientPort userWebClientPort;

    @Mock
    private NotificationWebClientPort notificationWebClientPort;

    @InjectMocks
    private MarkOrderReadyService service;

    @Test
    void shouldMarkOrderReadySuccessfully() {
        Order validatedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .customerId(20L)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .customerId(20L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(5L)
                .restaurantName("Restaurante Test")
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .dishId(1L)
                .dishName("Hamburguesa")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(20L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("+51900671048")
                .status(true)
                .roleName("CLIENTE")
                .build();

        NotificationResponse notificationResponse = NotificationResponse.builder()
                .phoneNumber("+51900671048")
                .message("Tu pedido está listo")
                .build();

        doNothing().when(assignOrderDomainValidator).validate(anyLong());
        when(markOrderReadyValidator.validate(anyLong(), anyString())).thenReturn(Mono.just(validatedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(orderDetail, orderDetail));
        when(userWebClientPort.findById(20L, "token-test")).thenReturn(Mono.just(userResponse));
        when(notificationWebClientPort.sendReadyNotification("+51900671048", "token-test"))
                .thenReturn(Mono.just(notificationResponse));
        when(orderPersistencePort.save(validatedOrder)).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(service.markReady(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(2, result.size());
                    Assertions.assertEquals(OrderStatus.READY, result.getFirst().getStatus());
                    Assertions.assertEquals(20L, result.getFirst().getCustomerId());
                })
                .verifyComplete();

        verify(notificationWebClientPort).sendReadyNotification("+51900671048", "token-test");
        verify(orderPersistencePort).save(validatedOrder);
    }

    @Test
    void shouldNotSaveOrderWhenNotificationFails() {
        Order validatedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.READY)
                .employeeAssignedId(30L)
                .customerId(20L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(5L)
                .restaurantName("Restaurante Test")
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .dishId(1L)
                .dishName("Hamburguesa")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(20L)
                .firstName("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("+51900671048")
                .status(true)
                .roleName("CLIENTE")
                .build();

        doNothing().when(assignOrderDomainValidator).validate(anyLong());
        when(markOrderReadyValidator.validate(anyLong(), anyString())).thenReturn(Mono.just(validatedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(orderDetail));
        when(userWebClientPort.findById(anyLong(), anyString())).thenReturn(Mono.just(userResponse));
        when(notificationWebClientPort.sendReadyNotification(anyString(), anyString()))
                .thenReturn(Mono.error(new ExternalServiceException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error interno del servicio de notificaciones"
                )));

        StepVerifier.create(service.markReady(100L, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(ExternalServiceException.class, error);
                    Assertions.assertEquals("Error interno del servicio de notificaciones", error.getMessage());
                })
                .verify();

        verify(notificationWebClientPort).sendReadyNotification(anyString(), anyString());
        verify(orderPersistencePort, never()).save(any());
    }

}
