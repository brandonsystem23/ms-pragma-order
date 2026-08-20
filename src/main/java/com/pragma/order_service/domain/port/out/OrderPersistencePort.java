package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.query.OrderDetail;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderPersistencePort {

    Mono<Boolean> existsByCustomerIdAndRestaurantIdAndStatusIn(Long customerId, Long restaurantId);

    Mono<Order> save(Order order);

    Mono<Order> findById(Long orderId);

    Flux<OrderDetail> findOrderDetailById(Long orderId);

    Mono<Long> countOrdersByRestaurantIdAndStatus(Long restaurantId, String status);

    Flux<Long> findOrderIdsByRestaurantIdAndStatus(Long restaurantId, String status, int page, int size);

    Flux<OrderDetail> findOrdersDetailByIds(List<Long> orderIds);
}
