package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateDishRegistrationValidator {

    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IRedisCachePort authSessionPort;

    public Mono<Dish> validate(Long dishId, String token, Boolean status) {
        return validateOwnerRole(token)
                .flatMap(authSession -> {
                    if (status != null) {
                        return findDish(dishId)
                                .flatMap(dish -> validateRestaurantOwnership(dish, authSession.userId())
                                        .thenReturn(dish));
                    }

                    return findDishActive(dishId)
                            .flatMap(dish -> validateRestaurantOwnership(dish, authSession.userId())
                                    .thenReturn(dish));
                }
                );
    }

    private Mono<AuthSession> validateOwnerRole(String token) {
        return authSessionPort.findByToken(token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_TOKEN,
                        DomainErrorMessages.INVALID_TOKEN
                )))
                .map(authSessionRedisValue -> AuthSession.builder()
                        .userId(authSessionRedisValue.userId())
                        .fullName(authSessionRedisValue.fullName())
                        .role(authSessionRedisValue.role())
                        .numberDocument(authSessionRedisValue.numberDocument())
                        .phone(authSessionRedisValue.phone())
                        .email(authSessionRedisValue.email())
                        .build())
                .flatMap(this::checkOwnerRole);
    }

    private Mono<AuthSession> checkOwnerRole(AuthSession authSession) {
        if (!RoleNames.OWNER.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.DISH_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }

    private Mono<Dish> findDish(Long dishId) {
        return dishPersistencePort.findById(dishId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.DISH_NOT_FOUND,
                        DomainErrorMessages.DISH_NOT_FOUND
                )));
    }

    private Mono<Dish> findDishActive(Long dishId) {
        return dishPersistencePort.findByIdAndStatusTrue(dishId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.DISH_NOT_FOUND,
                        DomainErrorMessages.DISH_NOT_FOUND
                )));
    }

    private Mono<Void> validateRestaurantOwnership(Dish dish, Long ownerId) {
        return restaurantPersistencePort.existByOwner(dish.getRestaurantId(), ownerId)
                .flatMap(isOwner -> Boolean.TRUE.equals(isOwner)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                        DomainErrorCode.INVALID_OWNER_RESTAURANT,
                        DomainErrorMessages.INVALID_RESTAURANT
                )));
    }
}
