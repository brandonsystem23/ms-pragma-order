package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateDishRegistrationValidator {

    private static final String OWNER_ROLE = "PROPIETARIO";

    private final DishPersistencePort dishPersistencePort;
    private final RestaurantPersistencePort restaurantPersistencePort;
    private final AuthSessionPort authSessionPort;

    public Mono<Dish> validate(Long dishId, String token) {
        return validateOwnerRole(token)
                .flatMap(authSession -> findDish(dishId)
                        .flatMap(dish -> validateRestaurantOwnership(dish, authSession.userId())
                                .thenReturn(dish))
                );
    }

    private Mono<AuthSession> validateOwnerRole(String token) {
        return authSessionPort.findByToken(token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_TOKEN,
                        DomainErrorMessages.INVALID_TOKEN
                )))
                .flatMap(this::checkOwnerRole);
    }

    private Mono<AuthSession> checkOwnerRole(AuthSession authSession) {
        if (!OWNER_ROLE.equals(authSession.role())) {
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
