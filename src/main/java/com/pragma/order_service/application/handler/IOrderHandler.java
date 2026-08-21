package com.pragma.order_service.application.handler;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import reactor.core.publisher.Mono;

public interface IOrderHandler {

    Mono<OrderResponse> updateStatus(Long orderId, UpdateOrderRequest request, String token);

    Mono<OrderResponse> create(CreateOrderRequest request, String token);

    Mono<PagedResponse<OrderResponse>> list(String token, String status, int page, int size);
}
