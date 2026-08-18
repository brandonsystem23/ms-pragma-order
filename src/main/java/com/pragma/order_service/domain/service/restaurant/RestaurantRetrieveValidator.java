package com.pragma.order_service.domain.service.restaurant;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RestaurantRetrieveValidator {

    private static final String CLIENT_ROLE = "CLIENTE";

    private final AuthSessionPort authSessionPort;

    public Mono<Void> validate(String token) {
        return authSessionPort.findByToken(token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_TOKEN,
                        DomainErrorMessages.INVALID_TOKEN
                )))
                .flatMap(this::checkClientRole)
                .then();
    }

    private Mono<AuthSession> checkClientRole(AuthSession authSession) {
        if (!CLIENT_ROLE.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.RESTAURANT_LIST_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }
}
