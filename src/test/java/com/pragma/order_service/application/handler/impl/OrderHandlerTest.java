package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateOrderItemRequest;
import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderItemQueryModel;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private ICreateOrderServicePort createOrderUseCase;

    @Mock
    private OrderDtoMapper orderDtoMapper;

    @Mock
    private IListOrdersServicePort listOrdersUseCase;

    @Mock
    private IUpdateOrderServicePort updateOrderStatusUseCase;

    @InjectMocks
    private OrderHandler orderApplicationService;

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

        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
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
                .customerId(50L)
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

        OrderItemResponse itemResponse1 = OrderItemResponse.builder()
                .dishId(10L)
                .name("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(30000))
                .build();

        OrderItemResponse itemResponse2 = OrderItemResponse.builder()
                .dishId(11L)
                .name("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .price(BigDecimal.valueOf(5000))
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .customerId(50L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(itemResponse1, itemResponse2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderDtoMapper.toCommand(any())).thenReturn(command);
        when(orderDtoMapper.toResponse(any(), anyList())).thenReturn(orderResponse);
        when(orderDtoMapper.toItemResponse(any())).thenReturn(itemResponse1).thenReturn(itemResponse2);
        when(createOrderUseCase.create(any(), anyString())).thenReturn(Mono.just(List.of(row1, row2)));

        StepVerifier.create(orderApplicationService.create(request, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.totalPrice());
                    Assertions.assertEquals(2, result.items().size());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.items().get(0).price());
                    Assertions.assertEquals(BigDecimal.valueOf(5000), result.items().get(1).price());
                })
                .verifyComplete();
    }

    @Test
    void shouldListOrdersSuccessfully() {
        OrderQueryModel orderQueryModel = OrderQueryModel.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Juan Perez")
                .restaurantId(1L)
                .nameRestaurant("El Buen Sabor")
                .status("PENDIENTE")
                .employeeAssignedId(null)
                .totalPrice(BigDecimal.valueOf(40000))
                .items(List.of(
                        OrderItemQueryModel.builder()
                                .dishId(10L)
                                .name("Pizza")
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(20000))
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PageResult<OrderQueryModel> pageResult = PageResult.<OrderQueryModel>builder()
                .content(List.of(orderQueryModel))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        OrderItemResponse itemResponse1 = OrderItemResponse.builder()
                .dishId(10L)
                .name("Pizza")
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(20000))
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Juan Perez")
                .restaurantId(1L)
                .nameRestaurant("El Buen Sabor")
                .status("PENDIENTE")
                .employeeAssignedId(null)
                .totalPrice(BigDecimal.valueOf(40000))
                .items(List.of(itemResponse1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(listOrdersUseCase.list(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(pageResult));

        when(orderDtoMapper.toResponse(any(OrderQueryModel.class))).thenReturn(orderResponse);

        StepVerifier.create(orderApplicationService.list("token-test", "PENDIENTE", 0, 10))
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals(BigDecimal.valueOf(40000), response.content().getFirst().totalPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(20000), response.content().getFirst().items().getFirst().price());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        UpdateOrderRequest request = new UpdateOrderRequest("ENTREGADO", "151370");
        UpdateOrderCommand command = new UpdateOrderCommand("ENTREGADO", "151370");

        when(orderDtoMapper.toUpdateStatusCommand(any())).thenReturn(command);
        when(updateOrderStatusUseCase.update(anyLong(), any(), anyString()))
                .thenReturn(Mono.just(100L));

        StepVerifier.create(orderApplicationService.updateStatus(100L, request, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals("Estado del pedido actualizado exitosamente", result.message());
                })
                .verifyComplete();
    }
}
