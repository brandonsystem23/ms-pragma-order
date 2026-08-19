package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateOrderItemRequest;
import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
                    Assertions.assertEquals("El buen sabor", result.nameRestaurant());
                    Assertions.assertEquals("PENDIENTE", result.status());
                    Assertions.assertEquals(2, result.items().size());
                    Assertions.assertEquals("Hamburguesa triple", result.items().get(0).name());
                    Assertions.assertEquals(BigDecimal.valueOf(2), result.items().get(0).quantity());
                    Assertions.assertEquals("Lomo saltado", result.items().get(1).name());
                    Assertions.assertEquals(BigDecimal.ONE, result.items().get(1).quantity());
                })
                .verifyComplete();
    }
}
