package com.pragma.order_service.infrastructure.out.postgres.adapter;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.infrastructure.out.postgres.mapper.RestaurantEntityMapper;
import com.pragma.order_service.infrastructure.out.postgres.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RestaurantPersistenceAdapter implements IRestaurantPersistencePort {

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

    @Override
    public Mono<Restaurant> findById(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .map(restaurantEntityMapper::toDomain);
    }

    @Override
    public Flux<Restaurant> findActiveRestaurantsOrdered(int page, int size) {
        int offset = page * size;
        return restaurantRepository.findActiveRestaurantsOrdered(size, offset)
                .map(restaurantEntityMapper::toDomain);
    }

    @Override
    public Mono<Long> countActiveRestaurants() {
        return restaurantRepository.countActiveRestaurants();
    }

    @Override
    public Mono<Long> findRestaurantIdByEmployeeId(Long employeeId) {
        return restaurantRepository.findRestaurantIdByEmployeeId(employeeId);
    }
}
