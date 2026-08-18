package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.RestaurantEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RestaurantRepository extends ReactiveCrudRepository<RestaurantEntity, Long> {

    Mono<Boolean> existsByNit(String nit);

    Mono<Boolean> existsByIdAndOwnerId(Long restaurantId, Long ownerId);

    @Query("""
    SELECT *
    FROM restaurant
    WHERE status = TRUE
    ORDER BY name ASC
    LIMIT :size OFFSET :offset
    """)
    Flux<RestaurantEntity> findActiveRestaurantsOrdered(int size, int offset);

    @Query("""
    SELECT COUNT(*)
    FROM restaurant
    WHERE status = TRUE
    """)
    Mono<Long> countActiveRestaurants();
}
