package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateDishRegistrationValidator {

    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    public Mono<Dish> findActiveDishAndValidateOwnership(Long dishId, Long ownerId) {
        return findActiveDishByIdOrFail(dishId)
                .flatMap(dish -> validateRestaurantOwnership(dish.getRestaurantId(), ownerId)
                        .thenReturn(dish));
    }

    public Mono<Dish> findDishAndValidateOwnership(Long dishId, Long ownerId) {
        return findDishByIdOrFail(dishId)
                .flatMap(dish -> validateRestaurantOwnership(dish.getRestaurantId(), ownerId)
                        .thenReturn(dish));
    }

    public Mono<Dish> findActiveDishByIdOrFail(Long dishId) {
        return dishPersistencePort.findByIdAndStatusTrue(dishId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.DISH_NOT_FOUND,
                        DomainErrorMessages.DISH_NOT_FOUND
                )));
    }

    public Mono<Dish> findDishByIdOrFail(Long dishId) {
        return dishPersistencePort.findById(dishId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.DISH_NOT_FOUND,
                        DomainErrorMessages.DISH_NOT_FOUND
                )));
    }

    public Mono<Void> validateRestaurantOwnership(Long restaurantId, Long ownerId) {
        return restaurantPersistencePort.existByOwner(restaurantId, ownerId)
                .flatMap(isOwner -> Boolean.TRUE.equals(isOwner)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                        DomainErrorCode.INVALID_OWNER_RESTAURANT,
                        DomainErrorMessages.INVALID_RESTAURANT
                )));
    }
}
