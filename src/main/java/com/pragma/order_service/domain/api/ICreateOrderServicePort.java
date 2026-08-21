package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICreateOrderServicePort {

    Mono<List<OrderDetail>> create(CreateOrderCommand createOrderCommand, String token);
}
