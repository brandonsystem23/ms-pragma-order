package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.query.OrderDetail;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AssignOrderUseCase {

    Mono<List<OrderDetail>> assign(Long orderId, String token);
}
