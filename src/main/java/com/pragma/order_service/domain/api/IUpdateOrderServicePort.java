package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IUpdateOrderServicePort {

    Mono<List<OrderDetail>> update(Long orderId, UpdateOrderCommand updateOrderCommand, String token);
}
