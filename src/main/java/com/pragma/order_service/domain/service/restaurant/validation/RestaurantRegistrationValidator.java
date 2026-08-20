package com.pragma.order_service.domain.service.restaurant.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.domain.port.out.UserWebClientPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RestaurantRegistrationValidator {

    private final RestaurantPersistencePort restaurantPersistencePort;
    private final AuthSessionPort authSessionPort;
    private final UserWebClientPort userWebClientPort;

    public Mono<Void> validate(String nit, Long ownerId, String token) {
        return validateAdminRole(token)
                .then(validateNit(nit))
                .then(validateOwner(ownerId, token));
    }

    private Mono<Void> validateAdminRole(String token) {
        return authSessionPort.findByToken(token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_TOKEN,
                        DomainErrorMessages.INVALID_TOKEN
                )))
                .flatMap(this::checkAdminRole)
                .then();
    }

    private Mono<Void> checkAdminRole(AuthSession authSession) {
        if (!RoleNames.ADMIN.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.RESTAURANT_ACCESS_DENIED
            ));
        }

        return Mono.empty();
    }

    private Mono<Void> validateNit(String nit) {
        return restaurantPersistencePort.existsByNit(nit)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DomainException(
                        DomainErrorCode.DUPLICATE_NIT,
                        DomainErrorMessages.DUPLICATE_NIT
                ))
                        : Mono.empty());
    }

    private Mono<Void> validateOwner(Long ownerId, String token) {
        return userWebClientPort.findById(ownerId, token)
                .map(userResponse -> UserSummary.builder()
                        .id(userResponse.id())
                        .firstName(userResponse.firstName())
                        .lastName(userResponse.lastName())
                        .email(userResponse.email())
                        .phone(userResponse.phone())
                        .status(userResponse.status())
                        .roleName(userResponse.roleName())
                        .build())
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.OWNER_NOT_FOUND,
                        DomainErrorMessages.OWNER_NOT_FOUND
                )))
                .flatMap(this::checkOwnerRole)
                .then();
    }

    private Mono<Void> checkOwnerRole(UserSummary userSummary) {
        if (!Boolean.TRUE.equals(userSummary.status())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.OWNER_NOT_FOUND,
                    DomainErrorMessages.OWNER_NOT_FOUND
            ));
        }

        if (!RoleNames.OWNER.equals(userSummary.roleName())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.INVALID_OWNER_ROLE,
                    DomainErrorMessages.INVALID_OWNER_ROLE
            ));
        }

        return Mono.empty();
    }
}
