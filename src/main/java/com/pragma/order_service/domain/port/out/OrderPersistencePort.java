package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderPersistencePort {

    Mono<Boolean> existsByCustomerIdAndRestaurantIdAndStatusIn(Long customerId, Long restaurantId);

    Mono<Order> save(Order order);

    Flux<OrderSummary> findOrderDetailById(Long orderId);

    Mono<Long> countOrdersByRestaurantIdAndStatus(Long restaurantId, String status);

    Flux<Long> findOrderIdsByRestaurantIdAndStatus(Long restaurantId, String status, int page, int size);

    Flux<OrderSummary> findOrdersDetailByIds(List<Long> orderIds);
}
