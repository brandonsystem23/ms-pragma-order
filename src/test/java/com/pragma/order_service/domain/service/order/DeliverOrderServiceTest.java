package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.DeliverOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.DeliverOrderValidator;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverOrderServiceTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private DeliverOrderValidator deliverOrderValidator;

    @Mock
    private DeliverOrderDomainValidator deliverOrderDomainValidator;

    @InjectMocks
    private DeliverOrderService service;

    @Test
    void shouldDeliverOrderSuccessfully() {
        Order validatedOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.DELIVERED)
                .employeeAssignedId(30L)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .status(OrderStatus.DELIVERED)
                .employeeAssignedId(30L)
                .build();

        OrderDetail orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("El Buen Sabor")
                .status(OrderStatus.DELIVERED)
                .employeeAssignedId(30L)
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(deliverOrderDomainValidator).validate(anyLong(), anyString());
        when(deliverOrderValidator.validate(anyLong(), anyString(), anyString())).thenReturn(Mono.just(validatedOrder));
        when(orderPersistencePort.save(validatedOrder)).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(orderDetail));

        StepVerifier.create(service.deliver(100L, "151370", "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.size());
                    Assertions.assertEquals(OrderStatus.DELIVERED, result.getFirst().getStatus());
                })
                .verifyComplete();
    }
}
