package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.OrderItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, Long> {
}
