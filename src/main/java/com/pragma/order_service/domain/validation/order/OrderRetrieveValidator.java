package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderRetrieveValidator {

    private final IRedisCachePort authSessionPort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    public Mono<Long> validate(String token) {
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
                .flatMap(this::checkEmployeeRole)
                .flatMap(authSession ->
                        restaurantPersistencePort.findRestaurantIdByEmployeeId(authSession.userId())
                                .switchIfEmpty(Mono.error(new DomainException(
                                        DomainErrorCode.EMPLOYEE_RESTAURANT_NOT_FOUND,
                                        DomainErrorMessages.EMPLOYEE_RESTAURANT_NOT_FOUND
                                )))
                );
    }

    private Mono<AuthSession> checkEmployeeRole(AuthSession authSession) {
        if (!RoleNames.EMPLOYEE.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_LIST_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }
}
