package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import com.pragma.order_service.domain.validation.order.OrderTraceabilityValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private ITraceabilityWebClientPort traceabilityWebClientPort;

    @Mock
    private OrderRegistrationValidator orderRegistrationValidator;

    @Mock
    private OrderDomainValidator orderDomainValidator;

    @Mock
    private OrderTraceabilityValidator orderTraceabilityValidator;

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

        Dish dish1 = Dish.builder()
                .id(10L)
                .price(BigDecimal.valueOf(30000))
                .restaurantId(1L)
                .build();

        Dish dish2 = Dish.builder()
                .id(11L)
                .price(BigDecimal.valueOf(5000))
                .restaurantId(1L)
                .build();

        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .dishPrice(BigDecimal.valueOf(30000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail row2 = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .dishId(11L)
                .dishName("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .dishPrice(BigDecimal.valueOf(5000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(
                        OrderItem.builder()
                                .id(1L)
                                .orderId(100L)
                                .dishId(10L)
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(30000))
                                .build(),
                        OrderItem.builder()
                                .id(2L)
                                .orderId(100L)
                                .dishId(11L)
                                .quantity(BigDecimal.ONE)
                                .price(BigDecimal.valueOf(5000))
                                .build()
                ))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(1L)
                .name("El buen sabor")
                .ownerId(5L)
                .build();

        TraceabilityRecord traceabilityRecord = TraceabilityRecord.builder()
                .id("trace-1")
                .orderId(100L)
                .build();

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(20L));
        when(dishPersistencePort.findByIds(anyList())).thenReturn(Flux.just(dish1, dish2));
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(row1, row2));
        when(orderTraceabilityValidator.validateAndGetRestaurant(anyList())).thenReturn(Mono.just(restaurant));
        when(traceabilityWebClientPort.create(any(), anyString())).thenReturn(Mono.just(traceabilityRecord));

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(2, result.size());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.getFirst().getTotalPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getFirst().getDishPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldBuildOrderWithPendingStatusAndTotalPriceBeforeSaving() {
        CreateOrderCommand command = new CreateOrderCommand(
                5L,
                List.of(new CreateOrderItemCommand(99L, BigDecimal.valueOf(3)))
        );

        Dish dish = Dish.builder()
                .id(99L)
                .price(BigDecimal.valueOf(10000))
                .restaurantId(5L)
                .build();

        LocalDateTime now = LocalDateTime.now();

        OrderDetail row = OrderDetail.builder()
                .orderId(100L)
                .customerId(33L)
                .customerName("Brandon Briones")
                .restaurantId(5L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(30000))
                .dishId(99L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(3))
                .dishPrice(BigDecimal.valueOf(10000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(5L)
                .name("El buen sabor")
                .ownerId(7L)
                .build();

        TraceabilityRecord traceabilityRecord = TraceabilityRecord.builder()
                .id("trace-2")
                .orderId(100L)
                .build();

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(33L));
        when(dishPersistencePort.findByIds(anyList())).thenReturn(Flux.just(dish));

        when(orderPersistencePort.save(any()))
                .thenAnswer(invocation -> {
                    Order orderToSave = invocation.getArgument(0);
                    orderToSave.setId(100L);
                    return Mono.just(orderToSave);
                });

        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(row));
        when(orderTraceabilityValidator.validateAndGetRestaurant(anyList())).thenReturn(Mono.just(restaurant));
        when(traceabilityWebClientPort.create(any(), anyString())).thenReturn(Mono.just(traceabilityRecord));

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.size());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getFirst().getTotalPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(10000), result.getFirst().getDishPrice());
                })
                .verifyComplete();

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderPersistencePort).save(captor.capture());

        Order orderSent = captor.getValue();
        Assertions.assertEquals(OrderStatus.PENDING, orderSent.getStatus());
        Assertions.assertEquals(BigDecimal.valueOf(30000), orderSent.getTotalPrice());
        Assertions.assertEquals(33L, orderSent.getCustomerId());
        Assertions.assertEquals(5L, orderSent.getRestaurantId());
        Assertions.assertEquals(1, orderSent.getItems().size());
        Assertions.assertEquals(BigDecimal.valueOf(10000), orderSent.getItems().getFirst().getPrice());
    }

    @Test
    void shouldReturnOrderDetailsWhenOrderDetailsAreEmpty() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.ONE))
        );

        Dish dish = Dish.builder()
                .id(10L)
                .price(BigDecimal.valueOf(15000))
                .restaurantId(1L)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(15000))
                .items(List.of(
                        OrderItem.builder()
                                .dishId(10L)
                                .quantity(BigDecimal.ONE)
                                .price(BigDecimal.valueOf(15000))
                                .build()
                ))
                .build();

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(20L));
        when(dishPersistencePort.findByIds(anyList())).thenReturn(Flux.just(dish));
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.empty());
        when(orderTraceabilityValidator.validateAndGetRestaurant(anyList())).thenReturn(Mono.empty());

        StepVerifier.create(service.create(command, "token-test"))
                .assertNext(result -> Assertions.assertTrue(result.isEmpty()))
                .verifyComplete();

        verify(traceabilityWebClientPort, never()).create(any(), anyString());
    }

    @Test
    void shouldFailWhenRestaurantNotFoundDuringTraceabilityCreation() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.ONE))
        );

        Dish dish = Dish.builder()
                .id(10L)
                .price(BigDecimal.valueOf(15000))
                .restaurantId(1L)
                .build();

        LocalDateTime now = LocalDateTime.now();

        OrderDetail detail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(15000))
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.ONE)
                .dishPrice(BigDecimal.valueOf(15000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        Order savedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.valueOf(15000))
                .items(List.of(
                        OrderItem.builder()
                                .dishId(10L)
                                .quantity(BigDecimal.ONE)
                                .price(BigDecimal.valueOf(15000))
                                .build()
                ))
                .build();

        doNothing().when(orderDomainValidator).validateForCreate(any());
        when(orderRegistrationValidator.validate(any(), any(), anyString())).thenReturn(Mono.just(20L));
        when(dishPersistencePort.findByIds(anyList())).thenReturn(Flux.just(dish));
        when(orderPersistencePort.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(detail));
        when(orderTraceabilityValidator.validateAndGetRestaurant(anyList()))
                .thenReturn(Mono.error(new DomainException(null, "El restaurante no existe")));

        StepVerifier.create(service.create(command, "token-test"))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }

}
