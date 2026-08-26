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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private ICreateOrderServicePort createOrderServicePort;

    @Mock
    private IListOrdersServicePort listOrdersServicePort;

    @Mock
    private IUpdateOrderServicePort updateOrderServicePort;

    @Mock
    private OrderDtoMapper orderDtoMapper;

    @InjectMocks
    private OrderHandler orderHandler;

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

        OrderQueryModel queryModel = OrderQueryModel.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Juan Perez")
                .restaurantId(1L)
                .nameRestaurant("El Buen Sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(
                        OrderItemQueryModel.builder().dishId(10L).name("Pizza").quantity(BigDecimal.valueOf(2)).price(BigDecimal.valueOf(30000)).build(),
                        OrderItemQueryModel.builder().dishId(11L).name("Lomo").quantity(BigDecimal.ONE).price(BigDecimal.valueOf(5000)).build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Juan Perez")
                .restaurantId(1L)
                .nameRestaurant("El Buen Sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(
                        OrderItemResponse.builder().dishId(10L).name("Pizza").quantity(BigDecimal.valueOf(2)).price(BigDecimal.valueOf(30000)).build(),
                        OrderItemResponse.builder().dishId(11L).name("Lomo").quantity(BigDecimal.ONE).price(BigDecimal.valueOf(5000)).build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderDtoMapper.toCommand(request)).thenReturn(command);
        when(createOrderServicePort.create(command, 20L, "token-test")).thenReturn(Mono.just(queryModel));
        when(orderDtoMapper.toResponse(queryModel)).thenReturn(response);

        StepVerifier.create(orderHandler.create(request, 20L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.totalPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldListOrdersSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        OrderQueryModel queryModel = OrderQueryModel.builder()
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
                .createdAt(now)
                .updatedAt(now)
                .build();

        PageResult<OrderQueryModel> pageResult = PageResult.<OrderQueryModel>builder()
                .content(List.of(queryModel))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Juan Perez")
                .restaurantId(1L)
                .nameRestaurant("El Buen Sabor")
                .status("PENDIENTE")
                .employeeAssignedId(null)
                .totalPrice(BigDecimal.valueOf(40000))
                .items(List.of(
                        OrderItemResponse.builder()
                                .dishId(10L)
                                .name("Pizza")
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(20000))
                                .build()
                ))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(listOrdersServicePort.list(30L, "PENDIENTE", 0, 10)).thenReturn(Mono.just(pageResult));
        when(orderDtoMapper.toResponse(queryModel)).thenReturn(response);

        StepVerifier.create(orderHandler.list(30L, "PENDIENTE", 0, 10))
                .assertNext(result -> {
                    Assertions.assertEquals(1, result.content().size());
                    Assertions.assertEquals(100L, result.content().getFirst().id());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        UpdateOrderRequest request = new UpdateOrderRequest("ENTREGADO", "151370");
        UpdateOrderCommand command = new UpdateOrderCommand("ENTREGADO", "151370");

        when(orderDtoMapper.toUpdateStatusCommand(request)).thenReturn(command);
        when(updateOrderServicePort.update(100L, command, 30L, "EMPLEADO", "Martin Lopez", "12345678", "token-test"))
                .thenReturn(Mono.just(100L));

        StepVerifier.create(orderHandler.updateStatus(
                        100L,
                        request,
                        30L,
                        "EMPLEADO",
                        "Martin Lopez",
                        "12345678",
                        "token-test"
                ))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals("Estado del pedido actualizado exitosamente", result.message());
                })
                .verifyComplete();
    }
}
