package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateOrderItemRequest;
import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.port.in.CreateOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private OrderDtoMapper orderDtoMapper;

    @Mock
    private OrderPersistencePort orderPersistencePort;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                List.of(
                        new CreateOrderItemRequest(10L, BigDecimal.valueOf(2)),
                        new CreateOrderItemRequest(11L, BigDecimal.ONE)
                )
        );

        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(
                        new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                        new CreateOrderItemCommand(11L, BigDecimal.ONE)
                )
        );

        Order savedOrder = Order.builder()
                .id(100L)
                .customerId(50L)
                .restaurantId(1L)
                .status(OrderStatus.PENDING)
                .items(List.of(
                        OrderItem.builder().id(1L).orderId(100L).dishId(10L).quantity(BigDecimal.valueOf(2)).build(),
                        OrderItem.builder().id(2L).orderId(100L).dishId(11L).quantity(BigDecimal.ONE).build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        LocalDateTime now = LocalDateTime.now();

        OrderSummary row1 = OrderSummary.builder()
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

        OrderSummary row2 = OrderSummary.builder()
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

        OrderItemResponse itemResponse1 = OrderItemResponse.builder()
                .dishId(10L)
                .name("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .build();

        OrderItemResponse itemResponse2 = OrderItemResponse.builder()
                .dishId(11L)
                .name("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .customerId(50L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("PENDIENTE")
                .items(List.of(itemResponse1, itemResponse2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderDtoMapper.toCommand(any()))
                .thenReturn(command);

        when(orderDtoMapper.toResponse(any(), anyList()))
                .thenReturn(orderResponse);

        when(orderDtoMapper.toItemResponse(any()))
                .thenReturn(itemResponse1)
                .thenReturn(itemResponse2);
        when(createOrderUseCase.create(any(), anyString())).thenReturn(Mono.just(savedOrder));
        when(orderPersistencePort.findOrderDetailById(anyLong())).thenReturn(Flux.just(row1, row2));

        StepVerifier.create(orderApplicationService.create(request, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(50L, result.customerId());
                    Assertions.assertEquals("Brandon Briones", result.nameCustomer());
                    Assertions.assertEquals(1L, result.restaurantId());
                    Assertions.assertEquals("El buen sabor", result.nameRestaurant());
                    Assertions.assertEquals("PENDIENTE", result.status());
                    Assertions.assertEquals(2, result.items().size());

                    Assertions.assertEquals(10L, result.items().getFirst().dishId());
                    Assertions.assertEquals("Hamburguesa triple", result.items().get(0).name());
                    Assertions.assertEquals(BigDecimal.valueOf(2), result.items().get(0).quantity());

                    Assertions.assertEquals(11L, result.items().get(1).dishId());
                    Assertions.assertEquals("Lomo saltado", result.items().get(1).name());
                    Assertions.assertEquals(BigDecimal.ONE, result.items().get(1).quantity());
                })
                .verifyComplete();
    }
}
