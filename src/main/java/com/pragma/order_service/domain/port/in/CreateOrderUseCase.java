package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CreateOrderUseCase {

    Mono<List<OrderSummary>> create(CreateOrderCommand command, String token);
}
