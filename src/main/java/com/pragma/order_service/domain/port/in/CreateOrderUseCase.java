package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import reactor.core.publisher.Mono;

public interface CreateOrderUseCase {

    Mono<Order> create(CreateOrderCommand command, String token);
}
