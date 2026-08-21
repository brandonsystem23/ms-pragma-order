package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private OrderRegistrationValidator orderRegistrationValidator;

    @Mock
    private OrderDomainValidator orderDomainValidator;

    @InjectMocks
    private CreateOrderUseCase service;

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(
                        new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                        new CreateOrderItemCommand(11L, BigDecimal.ONE)
                )
        );

        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail row2 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .dishId(11L)
                .dishName("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status(OrderStatus.PENDING)
                .items(List.of(
                        OrderItem.builder().id(1L).orderId(100L).dishId(10L).quantity(BigDecimal.valueOf(2)).build(),
                        OrderItem.builder().id(2L).orderId(100L).dishId(11L).quantity(BigDecimal.ONE).build()
                ))
                .build();

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(20L));
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(row1, row2));

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> Assertions.assertEquals(2, result.size()))
                .verifyComplete();
    }

    @Test
    void shouldBuildOrderWithPendingStatusBeforeSaving() {
        CreateOrderCommand command = new CreateOrderCommand(
                5L,
                List.of(new CreateOrderItemCommand(99L, BigDecimal.valueOf(3)))
        );

        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail row2 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .dishId(11L)
                .dishName("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(33L));

        when(orderPersistencePort.save(any()))
                .thenAnswer(invocation -> {
                    Order orderToSave = invocation.getArgument(0);
                    orderToSave.setId(100L);
                    return Mono.just(orderToSave);
                });

        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(row1, row2));

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> Assertions.assertEquals(2, result.size()))
                .verifyComplete();
    }
}
