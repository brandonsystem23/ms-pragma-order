package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.infrastructure.output.postgres.mapper.RestaurantEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RestaurantPersistenceAdapter implements RestaurantPersistencePort {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantEntityMapper restaurantEntityMapper;

    @Override
    public Mono<Boolean> existsByNit(String nit) {
        return restaurantRepository.existsByNit(nit);
    }

    @Override
    public Mono<Restaurant> save(Restaurant restaurant) {
        return restaurantRepository.save(restaurantEntityMapper.toEntity(restaurant))
                .map(restaurantEntityMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existByOwner(Long restaurantId, Long ownerId) {
        return restaurantRepository.existsByIdAndOwnerId(restaurantId, ownerId);
    }

    @Override
    public Mono<Boolean> existById(Long restaurantId) {
        return restaurantRepository.existsById(restaurantId);
    }
}
