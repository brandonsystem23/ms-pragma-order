package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.DishEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DishRepository extends ReactiveCrudRepository<DishEntity, Long> {

    Mono<Boolean> existsByNameIgnoreCase(String name);

    Mono<DishEntity> findByIdAndStatusTrue(Long id);

    @Query("""
        SELECT *
        FROM dish
        WHERE restaurant_id = :restaurantId
          AND status = TRUE
        ORDER BY name ASC
        LIMIT :size OFFSET :offset
        """)
    Flux<DishEntity> findActiveByRestaurantId(Long restaurantId, int size, int offset);

    @Query("""
        SELECT *
        FROM dish
        WHERE restaurant_id = :restaurantId
          AND status = TRUE
          AND category = :category
        ORDER BY name ASC
        LIMIT :size OFFSET :offset
        """)
    Flux<DishEntity> findActiveByRestaurantIdAndCategory(Long restaurantId, String category, int size, int offset);

    @Query("""
        SELECT COUNT(*)
        FROM dish
        WHERE restaurant_id = :restaurantId
          AND status = TRUE
        """)
    Mono<Long> countActiveByRestaurantId(Long restaurantId);

    @Query("""
        SELECT COUNT(*)
        FROM dish
        WHERE restaurant_id = :restaurantId
          AND status = TRUE
          AND category = :category
        """)
    Mono<Long> countActiveByRestaurantIdAndCategory(Long restaurantId, String category);
}
