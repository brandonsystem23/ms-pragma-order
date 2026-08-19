package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateOrderItemRequest;
import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.service.OrderApplicationService;
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
class OrderControllerTest {

    @Mock
    private OrderApplicationService orderApplicationService;

    @InjectMocks
    private OrderController orderController;

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest(
                1L,
                List.of(
                        new CreateOrderItemRequest(10L, BigDecimal.valueOf(2)),
                        new CreateOrderItemRequest(11L, BigDecimal.ONE)
                )
        );

        OrderResponse response = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("PENDIENTE")
                .employeeAssignedId(null)
                .items(List.of(
                        OrderItemResponse.builder()
                                .dishId(10L)
                                .name("Hamburguesa triple")
                                .quantity(BigDecimal.valueOf(2))
                                .build(),
                        OrderItemResponse.builder()
                                .dishId(11L)
                                .name("Lomo saltado")
                                .quantity(BigDecimal.ONE)
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderApplicationService.create(any(), anyString())).thenReturn(Mono.just(response));

        StepVerifier.create(orderController.create("Bearer token-test", request))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals(20L, result.customerId());
                    Assertions.assertEquals("Brandon Briones", result.nameCustomer());
                    Assertions.assertEquals(1L, result.restaurantId());
                    Assertions.assertEquals("PENDIENTE", result.status());
                    Assertions.assertNull(result.employeeAssignedId());
                })
                .verifyComplete();
    }

    @Test
    void shouldAssignOrderSuccessfully() {
        OrderResponse response = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Brandon Briones")
                .restaurantId(1L)
                .nameRestaurant("El buen sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .items(List.of(
                        OrderItemResponse.builder()
                                .dishId(10L)
                                .name("Hamburguesa triple")
                                .quantity(BigDecimal.valueOf(2))
                                .build(),
                        OrderItemResponse.builder()
                                .dishId(11L)
                                .name("Lomo saltado")
                                .quantity(BigDecimal.ONE)
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(orderApplicationService.assign(anyLong(), anyString())).thenReturn(Mono.just(response));

        StepVerifier.create(orderController.assign(100L, "Bearer token-test"))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.id());
                    Assertions.assertEquals("EN_PREPARACION", result.status());
                    Assertions.assertEquals(30L, result.employeeAssignedId());
                    Assertions.assertEquals(2, result.items().size());
                })
                .verifyComplete();
    }

    @Test
    void shouldListOrdersSuccessfully() {
        OrderResponse orderResponse = OrderResponse.builder()
                .id(100L)
                .customerId(20L)
                .nameCustomer("Juan Perez")
                .restaurantId(1L)
                .nameRestaurant("El Buen Sabor")
                .status("PENDIENTE")
                .employeeAssignedId(null)
                .items(List.of(
                        OrderItemResponse.builder()
                                .dishId(10L)
                                .name("Pizza")
                                .quantity(BigDecimal.valueOf(2))
                                .build()
                ))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PagedResponse<OrderResponse> pagedResponse = PagedResponse.<OrderResponse>builder()
                .content(List.of(orderResponse))
                .page(0)
                .size(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(orderApplicationService.list(anyString(), any(), anyInt(), anyInt()))
                .thenReturn(Mono.just(pagedResponse));

        StepVerifier.create(orderController.list("Bearer token-test", "PENDIENTE", 0, 10))
                .assertNext(response -> {
                    Assertions.assertEquals(1, response.content().size());
                    Assertions.assertEquals(100L, response.content().getFirst().id());
                    Assertions.assertEquals("PENDIENTE", response.content().getFirst().status());
                })
                .verifyComplete();
    }
}
