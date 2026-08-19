package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.OrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.OrderRegistrationValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private OrderRegistrationValidator orderRegistrationValidator;

    @Mock
    private OrderDomainValidator orderDomainValidator;

    @InjectMocks
    private CreateOrderService service;

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(
                        new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                        new CreateOrderItemCommand(11L, BigDecimal.ONE)
                )
        );

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

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(20L, result.getCustomerId());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                    Assertions.assertEquals("PENDIENTE", result.getStatus());
                    Assertions.assertEquals(2, result.getItems().size());
                    Assertions.assertEquals(BigDecimal.valueOf(2), result.getItems().get(0).getQuantity());
                    Assertions.assertEquals(BigDecimal.ONE, result.getItems().get(1).getQuantity());
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildOrderWithPendingStatusBeforeSaving() {
        CreateOrderCommand command = new CreateOrderCommand(
                5L,
                List.of(new CreateOrderItemCommand(99L, BigDecimal.valueOf(3)))
        );

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(33L));

        when(orderPersistencePort.save(any())).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            return Mono.just(orderToSave);
        });

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(33L, result.getCustomerId());
                    Assertions.assertEquals(5L, result.getRestaurantId());
                    Assertions.assertEquals("PENDIENTE", result.getStatus());
                    Assertions.assertEquals(1, result.getItems().size());
                    Assertions.assertEquals(99L, result.getItems().getFirst().getDishId());
                    Assertions.assertEquals(BigDecimal.valueOf(3), result.getItems().getFirst().getQuantity());
                })
                .verifyComplete();
    }
}
