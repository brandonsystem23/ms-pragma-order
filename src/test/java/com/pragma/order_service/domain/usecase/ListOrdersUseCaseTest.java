package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.validation.order.ListOrdersDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRetrieveValidator;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private OrderRetrieveValidator orderRetrieveValidator;

    @Mock
    private ListOrdersDomainValidator listOrdersDomainValidator;

    @InjectMocks
    private ListOrdersUseCase service;

    @Test
    void shouldListOrdersSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("El Buen Sabor")
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(50000))
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.valueOf(2))
                .dishPrice(BigDecimal.valueOf(20000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        doNothing().when(listOrdersDomainValidator).validate(OrderStatus.PENDING, 0, 10);
        when(orderRetrieveValidator.validateEmployeeHasRestaurantAssigned(30L)).thenReturn(Mono.just(1L));
        when(orderPersistencePort.countOrdersByRestaurantIdAndStatus(1L, OrderStatus.PENDING)).thenReturn(Mono.just(1L));
        when(orderPersistencePort.findOrderIdsByRestaurantIdAndStatus(1L, OrderStatus.PENDING, 0, 10)).thenReturn(Flux.just(100L));
        when(orderPersistencePort.findOrdersDetailByIds(java.util.List.of(100L))).thenReturn(Flux.just(row1));

        StepVerifier.create(service.list(30L, OrderStatus.PENDING, 0, 10))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals(1L, result.totalElements());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenOrderIdsAreEmpty() {
        doNothing().when(listOrdersDomainValidator)
                .validate(OrderStatus.PENDING, 0, 10);

        when(orderRetrieveValidator.validateEmployeeHasRestaurantAssigned(30L))
                .thenReturn(Mono.just(1L));

        when(orderPersistencePort.countOrdersByRestaurantIdAndStatus(
                1L, OrderStatus.PENDING))
                .thenReturn(Mono.just(0L));

        when(orderPersistencePort.findOrderIdsByRestaurantIdAndStatus(
                1L, OrderStatus.PENDING, 0, 10))
                .thenReturn(Flux.empty());

        StepVerifier.create(
                        service.list(30L, OrderStatus.PENDING, 0, 10)
                )
                .assertNext(result -> {
                    Assertions.assertTrue(result.content().isEmpty());
                    Assertions.assertEquals(0L, result.totalElements());
                    Assertions.assertEquals(0, result.totalPages());
                    Assertions.assertEquals(0, result.page());
                    Assertions.assertEquals(10, result.size());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroTotalPagesWhenThereAreNoOrders() {
        doNothing().when(listOrdersDomainValidator)
                .validate(OrderStatus.PENDING, 0, 10);

        when(orderRetrieveValidator.validateEmployeeHasRestaurantAssigned(30L))
                .thenReturn(Mono.just(1L));

        when(orderPersistencePort.countOrdersByRestaurantIdAndStatus(
                1L, OrderStatus.PENDING))
                .thenReturn(Mono.just(0L));

        when(orderPersistencePort.findOrderIdsByRestaurantIdAndStatus(
                1L, OrderStatus.PENDING, 0, 10))
                .thenReturn(Flux.just(100L));

        when(orderPersistencePort.findOrdersDetailByIds(
                java.util.List.of(100L)))
                .thenReturn(Flux.empty());

        StepVerifier.create(
                        service.list(30L, OrderStatus.PENDING, 0, 10)
                )
                .assertNext(result -> {
                    Assertions.assertTrue(result.content().isEmpty());
                    Assertions.assertEquals(0L, result.totalElements());
                    Assertions.assertEquals(0, result.totalPages());
                })
                .verifyComplete();
    }
}
