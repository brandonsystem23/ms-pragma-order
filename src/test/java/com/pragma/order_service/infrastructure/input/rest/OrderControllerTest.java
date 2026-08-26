package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateOrderItemRequest;
import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.UpdateOrderResponse;
import com.pragma.order_service.application.handler.IOrderHandler;
import com.pragma.order_service.infrastructure.security.jwt.AuthenticatedUser;
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
class OrderControllerTest {

    @Mock
    private IOrderHandler orderHandler;

    @InjectMocks
    private OrderController orderController;

    @Test
    void shouldCreateOrderSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                List.of(
                        new CreateOrderItemRequest(10L, BigDecimal.valueOf(2))
                )
        );

        OrderResponse response = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .items(List.of(
                        OrderItemResponse.builder()
                                .dishId(10L)
                                .quantity(BigDecimal.valueOf(2))
                                .price(BigDecimal.valueOf(30000))
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderHandler.create(request, 20L, "token-test")).thenReturn(Mono.just(response));

        StepVerifier.create(orderController.create(user, "Bearer token-test", request))
                .assertNext(result -> Assertions.assertEquals(100L, result.id()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(30L)
                .role("EMPLEADO")
                .fullName("Martin Lopez")
                .numberDocument("12345678")
                .build();

        UpdateOrderRequest request = new UpdateOrderRequest("ENTREGADO", "151370");

        UpdateOrderResponse response = UpdateOrderResponse.builder()
                .id(100L)
                .message("Estado del pedido actualizado exitosamente")
                .build();

        when(orderHandler.updateStatus(100L, request, 30L, "EMPLEADO", "Martin Lopez", "12345678", "token-test"))
                .thenReturn(Mono.just(response));

        StepVerifier.create(orderController.updateStatus(100L, user, "Bearer token-test", request))
                .assertNext(result -> Assertions.assertEquals(100L, result.id()))
                .verifyComplete();
    }

    @Test
    void shouldListOrdersSuccessfully() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(30L)
                .role("EMPLEADO")
                .build();

        PagedResponse<OrderResponse> response = PagedResponse.<OrderResponse>builder()
                .content(List.of(
                        OrderResponse.builder()
                                .id(100L)
                                .status("PENDIENTE")
                                .build()
                ))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(orderHandler.list(30L, "PENDIENTE", 0, 10)).thenReturn(Mono.just(response));

        StepVerifier.create(orderController.list(user, "PENDIENTE", 0, 10))
                .assertNext(result -> Assertions.assertEquals(1, result.content().size()))
                .verifyComplete();
    }
}
