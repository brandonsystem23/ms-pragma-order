package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AssignOrderUseCase {

    Mono<List<OrderSummary>> assign(Long orderId, String token);
}
