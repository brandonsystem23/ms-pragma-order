package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateOrderItemRequest;
import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderItemQueryModel;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.port.in.AssignOrderUseCase;
import com.pragma.order_service.domain.port.in.CreateOrderUseCase;
import com.pragma.order_service.domain.port.in.ListOrdersUseCase;
import com.pragma.order_service.domain.port.in.MarkOrderReadyUseCase;
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
class OrderApplicationServiceTest {

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private OrderDtoMapper orderDtoMapper;

    @Mock
    private ListOrdersUseCase listOrdersUseCase;

    @Mock
    private AssignOrderUseCase assignOrderUseCase;

    @Mock
    private MarkOrderReadyUseCase markOrderReadyUseCase;


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

        when(orderDtoMapper.toCommand(any())).thenReturn(command);
        when(orderDtoMapper.toResponse(any(), anyList())).thenReturn(orderResponse);
        when(orderDtoMapper.toItemResponse(any())).thenReturn(itemResponse1).thenReturn(itemResponse2);
        when(createOrderUseCase.create(any(), anyString())).thenReturn(Mono.just(List.of(row1, row2)));

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
                .items(List.of(
                        OrderItemQueryModel.builder()
                                .dishId(10L)
                                .name("Pizza")
                                .quantity(BigDecimal.valueOf(2))
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
                .name("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .customerId(50L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("PENDIENTE")
                .employeeAssignedId(30L)
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
                    Assertions.assertEquals(1L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                    Assertions.assertEquals("PENDIENTE", response.content().getFirst().status());
                })
                .verifyComplete();
    }

    @Test
    void shouldAssignOrderSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
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
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
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
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .items(List.of(itemResponse1, itemResponse2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(assignOrderUseCase.assign(100L, "token-test"))
                .thenReturn(Mono.just(List.of(row1, row2)));

        when(orderDtoMapper.toItemResponse(any()))
                .thenReturn(itemResponse1)
                .thenReturn(itemResponse2);

        when(orderDtoMapper.toResponse(any(), anyList()))
                .thenReturn(orderResponse);

        StepVerifier.create(orderApplicationService.assign(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(50L, result.customerId());
                    Assertions.assertEquals("Brandon Briones", result.nameCustomer());
                    Assertions.assertEquals(1L, result.restaurantId());
                    Assertions.assertEquals("El buen sabor", result.nameRestaurant());
                    Assertions.assertEquals("EN_PREPARACION", result.status());
                    Assertions.assertEquals(30L, result.employeeAssignedId());
                    Assertions.assertEquals(2, result.items().size());
                    Assertions.assertEquals(10L, result.items().getFirst().dishId());
                    Assertions.assertEquals("Hamburguesa triple", result.items().getFirst().name());
                    Assertions.assertEquals(BigDecimal.valueOf(2), result.items().getFirst().quantity());
                    Assertions.assertEquals(11L, result.items().get(1).dishId());
                    Assertions.assertEquals("Lomo saltado", result.items().get(1).name());
                    Assertions.assertEquals(BigDecimal.ONE, result.items().get(1).quantity());

                })
                .verifyComplete();
    }

    @Test
    void shouldMarkOrderReadySuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        OrderDetail row1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(50L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("LISTO")
                .employeeAssignedId(30L)
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderItemResponse itemResponse1 = OrderItemResponse.builder()
                .dishId(10L)
                .name("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .build();

        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .customerId(50L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("LISTO")
                .employeeAssignedId(30L)
                .items(List.of(itemResponse1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(markOrderReadyUseCase.markReady(100L, "token-test"))
                .thenReturn(Mono.just(List.of(row1)));

        when(orderDtoMapper.toItemResponse(any())).thenReturn(itemResponse1);
        when(orderDtoMapper.toResponse(any(), anyList())).thenReturn(orderResponse);

        StepVerifier.create(orderApplicationService.markReady(100L, "token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals("LISTO", result.status());
                    Assertions.assertEquals(30L, result.employeeAssignedId());
                })
                .verifyComplete();
    }
}
