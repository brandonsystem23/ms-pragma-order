package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.infrastructure.output.postgres.mapper.DishEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class DishPersistenceAdapter implements DishPersistencePort {

    private final DishRepository dishRepository;
    private final DishEntityMapper dishEntityMapper;

    @Override
    public Mono<Boolean> existsByName(String name) {
        return dishRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public Mono<Dish> save(Dish restaurant) {
        return dishRepository.save(dishEntityMapper.toEntity(restaurant))
                .map(dishEntityMapper::toDomain);
    }

    @Override
    public Mono<Dish> findByIdAndStatusTrue(Long dishId) {
        return dishRepository.findByIdAndStatusTrue(dishId)
                .map(dishEntityMapper::toDomain);
    }

    @Override
    public Mono<Dish> findById(Long dishId) {
        return dishRepository.findById(dishId)
                .map(dishEntityMapper::toDomain);
    }

    @Override
    public Flux<Dish> findActiveByRestaurantId(Long restaurantId, int page, int size) {
        int offset = page * size;
        return dishRepository.findActiveByRestaurantId(restaurantId, size, offset)
                .map(dishEntityMapper::toDomain);
    }

    @Override
    public Flux<Dish> findActiveByRestaurantIdAndCategory(Long restaurantId, String category, int page, int size) {
        int offset = page * size;
        return dishRepository.findActiveByRestaurantIdAndCategory(restaurantId, category, size, offset)
                .map(dishEntityMapper::toDomain);
    }

    @Override
    public Mono<Long> countActiveByRestaurantId(Long restaurantId) {
        return dishRepository.countActiveByRestaurantId(restaurantId);
    }

    @Override
    public Mono<Long> countActiveByRestaurantIdAndCategory(Long restaurantId, String category) {
        return dishRepository.countActiveByRestaurantIdAndCategory(restaurantId, category);
    }
}
