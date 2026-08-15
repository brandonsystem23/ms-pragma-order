package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.infrastructure.output.postgres.mapper.DishEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class DishPersistenceAdapter implements DishPersistencePort {

    private final DishRepository dishRepository;
    private final DishEntityMapper dishEntityMapper;

    @Override
    public Mono<Boolean> existsByName(String name) {
        return dishRepository.existsByName(name);
    }

    @Override
    public Mono<Dish> save(Dish restaurant) {
        return dishRepository.save(dishEntityMapper.toEntity(restaurant))
                .map(dishEntityMapper::toDomain);
    }

}
