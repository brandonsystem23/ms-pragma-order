package com.pragma.order_service.domain.service.restaurant.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RestaurantRetrieveValidator {

    private final AuthSessionPort authSessionPort;

    public Mono<Void> validate(String token) {
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
                .flatMap(this::checkClientRole)
                .then();
    }

    private Mono<AuthSession> checkClientRole(AuthSession authSession) {
        if (!RoleNames.CLIENT.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.RESTAURANT_LIST_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }
}
