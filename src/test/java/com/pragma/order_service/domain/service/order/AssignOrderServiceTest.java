package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.AssignOrderValidator;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
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
class AssignOrderServiceTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private AssignOrderValidator assignOrderValidator;

    @Mock
    private AssignOrderDomainValidator assignOrderDomainValidator;

    @InjectMocks
    private AssignOrderService service;

    @Test
    void shouldAssignOrderSuccessfully() {
        Order validatedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(30L)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.IN_PREPARATION)
                .employeeAssignedId(30L)
                .build();

        OrderSummary orderSummary = OrderSummary.builder()
                .orderId(savedOrder.getId())
                .customerId(10L)
                .customerName("Brandon Rojas")
                .restaurantId(savedOrder.getRestaurantId())
                .restaurantName("Restaurante Test")
                .status(savedOrder.getStatus())
                .employeeAssignedId(savedOrder.getEmployeeAssignedId())
                .dishId(1L)
                .dishName("Hamburguesa")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(assignOrderDomainValidator).validate(anyLong());
        when(assignOrderValidator.validate(anyLong(), anyString())).thenReturn(Mono.just(validatedOrder));
        when(orderPersistencePort.save(validatedOrder)).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(orderSummary));

        StepVerifier.create(service.assign(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.size());
                })
                .verifyComplete();
    }

    @Test
    void shouldChangeStatusToInPreparationBeforeSaving() {
        Order validatedOrder = Order.builder()
                .id(100L)
                .restaurantId(5L)
                .status(OrderStatus.PENDING)
                .employeeAssignedId(30L)
                .build();

        OrderSummary orderSummary = OrderSummary.builder()
                .orderId(validatedOrder.getId())
                .customerId(10L)
                .customerName("Brandon Rojas")
                .restaurantId(validatedOrder.getRestaurantId())
                .restaurantName("Restaurante Test")
                .status(validatedOrder.getStatus())
                .employeeAssignedId(validatedOrder.getEmployeeAssignedId())
                .dishId(1L)
                .dishName("Hamburguesa")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doNothing().when(assignOrderDomainValidator).validate(anyLong());
        when(assignOrderValidator.validate(anyLong(), anyString())).thenReturn(Mono.just(validatedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(orderSummary));
        when(orderPersistencePort.save(validatedOrder))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.assign(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.size());
                })
                .verifyComplete();
    }
}
