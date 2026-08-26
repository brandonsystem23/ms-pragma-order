package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import reactor.core.publisher.Mono;

public interface ICreateOrderServicePort {

    Mono<OrderQueryModel> create(CreateOrderCommand createOrderCommand, Long customerId, String token);
}
