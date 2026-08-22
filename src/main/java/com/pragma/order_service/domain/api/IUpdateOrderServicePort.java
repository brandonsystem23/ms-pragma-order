package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import reactor.core.publisher.Mono;

public interface IUpdateOrderServicePort {

    Mono<Long> update(Long orderId, UpdateOrderCommand updateOrderCommand, String token);
}
