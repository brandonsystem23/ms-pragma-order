package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import reactor.core.publisher.Mono;

public interface ListOrdersUseCase {

    Mono<PagedResponse<OrderResponse>> list(String token, String status, int page, int size);
}
