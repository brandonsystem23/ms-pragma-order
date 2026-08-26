package com.pragma.order_service.application.handler;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.UpdateOrderResponse;
import reactor.core.publisher.Mono;

public interface IOrderHandler {

    Mono<UpdateOrderResponse> updateStatus(Long orderId, UpdateOrderRequest request, Long userId, String role,
                                           String fullName, String numberDocument, String token);

    Mono<OrderResponse> create(CreateOrderRequest request, Long customerId, String token);

    Mono<PagedResponse<OrderResponse>> list(Long employeeId, String status, int page, int size);
}
