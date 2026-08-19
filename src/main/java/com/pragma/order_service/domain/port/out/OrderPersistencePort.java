package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderPersistencePort {

    Mono<Boolean> existsByCustomerIdAndRestaurantIdAndStatusIn(Long customerId, Long restaurantId);

    Mono<Order> save(Order order);

    Flux<OrderSummary> findOrderDetailById(Long orderId);
}
