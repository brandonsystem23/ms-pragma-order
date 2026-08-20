package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.ListOrdersDomainValidator;
import com.pragma.order_service.domain.service.order.validation.OrderRetrieveValidator;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListOrdersServiceTest {

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @Mock
    private OrderRetrieveValidator orderRetrieveValidator;

    @Mock
    private ListOrdersDomainValidator listOrdersDomainValidator;

    @InjectMocks
    private ListOrdersService service;

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
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail row2 = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("El Buen Sabor")
                .status(OrderStatus.PENDING)
                .dishId(11L)
                .dishName("Hamburguesa")
                .quantity(BigDecimal.ONE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        doNothing().when(listOrdersDomainValidator).validate(any(), anyInt(), anyInt());
        when(orderRetrieveValidator.validate(anyString())).thenReturn(Mono.just(1L));
        when(orderPersistencePort.countOrdersByRestaurantIdAndStatus(anyLong(), any()))
                .thenReturn(Mono.just(1L));
        when(orderPersistencePort.findOrderIdsByRestaurantIdAndStatus(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(100L));
        when(orderPersistencePort.findOrdersDetailByIds(any()))
                .thenReturn(Flux.just(row1, row2));

        StepVerifier.create(service.list("token-test", OrderStatus.PENDING, 0, 10))
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals(1L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                    Assertions.assertEquals(0, response.page());
                    Assertions.assertEquals(10, response.size());

                    OrderQueryModel order = response.content().getFirst();
                    Assertions.assertEquals(100L, order.id());
                    Assertions.assertEquals(20L, order.customerId());
                    Assertions.assertEquals("Juan Perez", order.nameCustomer());
                    Assertions.assertEquals(1L, order.restaurantId());
                    Assertions.assertEquals("El Buen Sabor", order.nameRestaurant());
                    Assertions.assertEquals(OrderStatus.PENDING, order.status());
                    Assertions.assertEquals(2, order.items().size());

                    Assertions.assertEquals(10L, order.items().getFirst().dishId());
                    Assertions.assertEquals("Pizza", order.items().get(0).name());
                    Assertions.assertEquals(BigDecimal.valueOf(2), order.items().get(0).quantity());

                    Assertions.assertEquals(11L, order.items().get(1).dishId());
                    Assertions.assertEquals("Hamburguesa", order.items().get(1).name());
                    Assertions.assertEquals(BigDecimal.ONE, order.items().get(1).quantity());
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyPageWhenNoOrdersExist() {
        doNothing().when(listOrdersDomainValidator).validate(any(), anyInt(), anyInt());
        when(orderRetrieveValidator.validate(anyString())).thenReturn(Mono.just(1L));
        when(orderPersistencePort.countOrdersByRestaurantIdAndStatus(anyLong(), any()))
                .thenReturn(Mono.just(0L));
        when(orderPersistencePort.findOrderIdsByRestaurantIdAndStatus(anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.list("token-test", OrderStatus.PENDING, 0, 10))
                .assertNext(response -> {
                    Assertions.assertTrue(response.content().isEmpty());
                    Assertions.assertEquals(0L, response.totalElements());
                    Assertions.assertEquals(0, response.totalPages());
                    Assertions.assertEquals(0, response.page());
                    Assertions.assertEquals(10, response.size());
                })
                .verifyComplete();
    }
}
