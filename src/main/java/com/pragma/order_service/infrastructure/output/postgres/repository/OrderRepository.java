package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.OrderEntity;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {

    @Query("""
        SELECT EXISTS (
            SELECT 1
            FROM orders
            WHERE customer_id = :customerId AND restaurant_id = :restaurantId
              AND status IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO')
        )
        """)
    Mono<Boolean> existsActiveOrderByCustomerId(Long customerId, Long restaurantId);

    @Query("""
        SELECT
            o.id AS order_id,
            u.id AS customer_id,
            CONCAT(u.first_name, ' ', u.last_name) AS customer_name,
            o.restaurant_id AS restaurant_id,
            r.name AS restaurant_name,
            o.status AS status,
            oi.dish_id AS dish_id,
            d.name AS dish_name,
            oi.quantity AS quantity,
            o.created_at AS created_at,
            o.updated_at AS updated_at
        FROM orders o
        INNER JOIN restaurant r ON r.id = o.restaurant_id
        INNER JOIN order_item oi ON oi.order_id = o.id
        INNER JOIN dish d ON d.id = oi.dish_id
        INNER JOIN users u ON u.id = o.customer_id
        WHERE o.id = :orderId
        ORDER BY oi.id ASC
        """)
    Flux<OrderSummary> findOrderDetailById(Long orderId);
}
