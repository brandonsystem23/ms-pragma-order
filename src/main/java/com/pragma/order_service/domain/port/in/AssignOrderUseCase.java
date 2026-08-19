package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.Order;
import reactor.core.publisher.Mono;

public interface AssignOrderUseCase {

    Mono<Order> assign(Long orderId, String token);
}
