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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderHandlerTest {

    @Mock
    private ICreateOrderServicePort createOrderUseCase;

    @Mock
    private IListOrdersServicePort listOrdersUseCase;

    @Mock
    private IUpdateOrderServicePort updateOrderStatusUseCase;

    @Mock
    private OrderDtoMapper orderDtoMapper;

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

        OrderQueryModel orderQueryModel = OrderQueryModel.builder()
                .id(100L)
                .customerId(50L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(
                        OrderItemQueryModel.builder()
                                .dishId(10L)
                                .name("Hamburguesa triple")
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(30000))
                                .build(),
                        OrderItemQueryModel.builder()
                                .dishId(11L)
                                .name("Lomo saltado")
                                .quantity(BigDecimal.ONE)
                                .price(BigDecimal.valueOf(5000))
                                .build()
                ))
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

        when(orderDtoMapper.toCommand(request))
                .thenReturn(command);

        when(createOrderUseCase.create(command, "token-test"))
                .thenReturn(Mono.just(orderQueryModel));

        when(orderDtoMapper.toResponse(orderQueryModel))
                .thenReturn(orderResponse);

        StepVerifier.create(
                        orderApplicationService.create(request, "token-test")
                )
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(
                            BigDecimal.valueOf(65000),
                            result.totalPrice()
                    );
                    Assertions.assertEquals(2, result.items().size());
                    Assertions.assertEquals(
                            BigDecimal.valueOf(30000),
                            result.items().get(0).price()
                    );
                    Assertions.assertEquals(
                            BigDecimal.valueOf(5000),
                            result.items().get(1).price()
                    );
                })
                .verifyComplete();

        verify(orderDtoMapper).toCommand(request);
        verify(createOrderUseCase).create(command, "token-test");
        verify(orderDtoMapper).toResponse(orderQueryModel);
    }

    @Test
    void shouldListOrdersSuccessfully() {

        LocalDateTime now = LocalDateTime.now();

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
                .createdAt(now)
                .updatedAt(now)
                .build();

        PageResult<OrderQueryModel> pageResult = PageResult.<OrderQueryModel>builder()
                .content(List.of(orderQueryModel))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        OrderItemResponse itemResponse = OrderItemResponse.builder()
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
                .items(List.of(itemResponse))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(listOrdersUseCase.list(
                "token-test",
                "PENDIENTE",
                0,
                10
        )).thenReturn(Mono.just(pageResult));

        when(orderDtoMapper.toResponse(orderQueryModel))
                .thenReturn(orderResponse);

        StepVerifier.create(
                        orderApplicationService.list(
                                "token-test",
                                "PENDIENTE",
                                0,
                                10
                        )
                )
                .assertNext(response -> {
                    Assertions.assertEquals(
                            1,
                            response.content().size()
                    );
                    Assertions.assertEquals(
                            100L,
                            response.content().getFirst().id()
                    );
                    Assertions.assertEquals(
                            BigDecimal.valueOf(40000),
                            response.content().getFirst().totalPrice()
                    );
                    Assertions.assertEquals(
                            BigDecimal.valueOf(20000),
                            response.content()
                                    .getFirst()
                                    .items()
                                    .getFirst()
                                    .price()
                    );
                    Assertions.assertEquals(0, response.page());
                    Assertions.assertEquals(10, response.size());
                    Assertions.assertEquals(1L, response.totalElements());
                    Assertions.assertEquals(1, response.totalPages());
                })
                .verifyComplete();

        verify(listOrdersUseCase).list(
                "token-test",
                "PENDIENTE",
                0,
                10
        );

        verify(orderDtoMapper).toResponse(orderQueryModel);
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {

        UpdateOrderRequest request =
                new UpdateOrderRequest("ENTREGADO", "151370");

        UpdateOrderCommand command =
                new UpdateOrderCommand("ENTREGADO", "151370");

        when(orderDtoMapper.toUpdateStatusCommand(request))
                .thenReturn(command);

        when(updateOrderStatusUseCase.update(
                100L,
                command,
                "token-test"
        )).thenReturn(Mono.just(100L));

        StepVerifier.create(
                        orderApplicationService.updateStatus(
                                100L,
                                request,
                                "token-test"
                        )
                )
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(
                            "Estado del pedido actualizado exitosamente",
                            result.message()
                    );
                })
                .verifyComplete();

        verify(orderDtoMapper).toUpdateStatusCommand(request);

        verify(updateOrderStatusUseCase).update(
                100L,
                command,
                "token-test"
        );
    }

    @Test
    void shouldPropagateErrorWhenCreateOrderFails() {

        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                List.of(
                        new CreateOrderItemRequest(
                                10L,
                                BigDecimal.ONE
                        )
                )
        );

        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(
                        new CreateOrderItemCommand(
                                10L,
                                BigDecimal.ONE
                        )
                )
        );

        RuntimeException exception =
                new RuntimeException("Error creating order");

        when(orderDtoMapper.toCommand(request))
                .thenReturn(command);

        when(createOrderUseCase.create(
                command,
                "token-test"
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        orderApplicationService.create(
                                request,
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertSame(exception, error)
                )
                .verify();

        verify(orderDtoMapper).toCommand(request);
        verify(createOrderUseCase).create(
                command,
                "token-test"
        );
    }

    @Test
    void shouldPropagateErrorWhenListOrdersFails() {

        RuntimeException exception =
                new RuntimeException("Error listing orders");

        when(listOrdersUseCase.list(
                "token-test",
                "PENDIENTE",
                0,
                10
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        orderApplicationService.list(
                                "token-test",
                                "PENDIENTE",
                                0,
                                10
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertSame(exception, error)
                )
                .verify();

        verify(listOrdersUseCase).list(
                "token-test",
                "PENDIENTE",
                0,
                10
        );
    }

    @Test
    void shouldPropagateErrorWhenUpdateStatusFails() {

        UpdateOrderRequest request =
                new UpdateOrderRequest("ENTREGADO", "151370");

        UpdateOrderCommand command =
                new UpdateOrderCommand("ENTREGADO", "151370");

        RuntimeException exception =
                new RuntimeException("Error updating order");

        when(orderDtoMapper.toUpdateStatusCommand(request))
                .thenReturn(command);

        when(updateOrderStatusUseCase.update(
                100L,
                command,
                "token-test"
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(
                        orderApplicationService.updateStatus(
                                100L,
                                request,
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error ->
                        Assertions.assertSame(exception, error)
                )
                .verify();

        verify(orderDtoMapper).toUpdateStatusCommand(request);

        verify(updateOrderStatusUseCase).update(
                100L,
                command,
                "token-test"
        );
    }
}