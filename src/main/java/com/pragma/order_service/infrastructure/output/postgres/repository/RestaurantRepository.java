package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.RestaurantEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RestaurantRepository extends ReactiveCrudRepository<RestaurantEntity, Long> {

    Mono<Boolean> existsByNit(String nit);
}
