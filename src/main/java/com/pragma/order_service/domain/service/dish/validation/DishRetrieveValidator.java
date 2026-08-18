package com.pragma.order_service.domain.service.dish.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DishRetrieveValidator {

    private static final String CLIENT_ROLE = "CLIENTE";
    private final RestaurantPersistencePort restaurantPersistencePort;
    private final AuthSessionPort authSessionPort;

    public Mono<Void> validate(String token, Long restaurantId) {
        return authSessionPort.findByToken(token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_TOKEN,
                        DomainErrorMessages.INVALID_TOKEN
                )))
                .flatMap(this::checkClientRole)
                .then(validateRestaurantExists(restaurantId));
    }

    private Mono<AuthSession> checkClientRole(AuthSession authSession) {
        if (!CLIENT_ROLE.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.DISH_LIST_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }

    private Mono<Void> validateRestaurantExists(Long restaurantId) {
        return restaurantPersistencePort.existById(restaurantId)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                                DomainErrorCode.RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.RESTAURANT_NOT_FOUND
                )));
    }
}
