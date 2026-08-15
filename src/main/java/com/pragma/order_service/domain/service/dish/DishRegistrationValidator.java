package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DishRegistrationValidator {

    private static final String OWNER_ROLE = "PROPIETARIO";

    private final RestaurantPersistencePort restaurantPersistencePort;
    private final DishPersistencePort dishPersistencePort;
    private final AuthSessionPort authSessionPort;

    public Mono<Void> validate(String name, Long restaurantId, String token) {
        return validateOwnerRole(token)
                .flatMap(authSession -> validateRestaurant(restaurantId, authSession.userId()))
                .then(validateName(name));

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

    private Mono<Void> validateName(String name) {
        return dishPersistencePort.existsByName(name)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DomainException(
                        DomainErrorCode.DUPLICATE_NAME,
                        DomainErrorMessages.DUPLICATE_NAME
                ))
                        : Mono.empty());
    }

    private Mono<Void> validateRestaurant(Long restaurantId, Long ownerId) {
        return restaurantPersistencePort.existById(restaurantId)
                .flatMap(existsRestaurant -> {
                    if (Boolean.FALSE.equals(existsRestaurant)) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.RESTAURANT_NOT_FOUND,
                                DomainErrorMessages.RESTAURANT_NOT_FOUND
                        ));
                    }

                    return restaurantPersistencePort.existByOwner(restaurantId, ownerId);
                })
                .flatMap(existsByOwner -> {
                    if (Boolean.FALSE.equals(existsByOwner)) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.INVALID_OWNER_RESTAURANT,
                                DomainErrorMessages.INVALID_RESTAURANT
                        ));
                    }

                    return Mono.empty();
                });
    }

}
