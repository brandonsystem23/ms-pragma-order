package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.DishEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DishRepository extends ReactiveCrudRepository<DishEntity, Long> {

    Mono<Boolean> existsByNameIgnoreCase(String name);

}
