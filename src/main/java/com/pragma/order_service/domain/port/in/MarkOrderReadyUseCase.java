package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.query.OrderDetail;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MarkOrderReadyUseCase {

    Mono<List<OrderDetail>> markReady(Long orderId, String token);
}
