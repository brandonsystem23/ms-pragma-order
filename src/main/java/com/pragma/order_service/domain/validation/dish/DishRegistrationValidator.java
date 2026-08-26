package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DishRegistrationValidator {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IDishPersistencePort dishPersistencePort;

    public Mono<Void> validateDishCreationRules(String name, Long restaurantId, Long ownerId) {
        return validateRestaurantExists(restaurantId)
                .then(Mono.defer(() -> validateRestaurantOwnership(restaurantId, ownerId)))
                .then(Mono.defer(() -> validateDishNameDoesNotExist(name)));
    }

    public Mono<Void> validateDishNameDoesNotExist(String name) {
        return dishPersistencePort.existsByName(name)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DomainException(
                        DomainErrorCode.DUPLICATE_NAME,
                        DomainErrorMessages.DUPLICATE_NAME
                ))
                        : Mono.empty());
    }

    public Mono<Void> validateRestaurantExists(Long restaurantId) {
        return restaurantPersistencePort.existById(restaurantId)
                .flatMap(existsRestaurant -> Boolean.TRUE.equals(existsRestaurant)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                        DomainErrorCode.RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.RESTAURANT_NOT_FOUND
                )));
    }

    public Mono<Void> validateRestaurantOwnership(Long restaurantId, Long ownerId) {
        return restaurantPersistencePort.existByOwner(restaurantId, ownerId)
                .flatMap(existsByOwner -> Boolean.TRUE.equals(existsByOwner)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                        DomainErrorCode.INVALID_OWNER_RESTAURANT,
                        DomainErrorMessages.INVALID_RESTAURANT
                )));
    }
}
