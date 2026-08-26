package com.pragma.order_service.domain.validation.restaurant;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RestaurantRegistrationValidator {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserWebClientPort userWebClientPort;

    public Mono<Void> validateRestaurantCreationRules(String nit, Long ownerId, String token) {
        return validateNitDoesNotExist(nit)
                .then(Mono.defer(() -> findOwnerByIdOrFail(ownerId, token)))
                .flatMap(owner -> validateOwnerIsActive(owner)
                        .then(Mono.defer(() -> validateOwnerHasOwnerRole(owner))));
    }

    public Mono<Void> validateNitDoesNotExist(String nit) {
        return restaurantPersistencePort.existsByNit(nit)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DomainException(
                        DomainErrorCode.DUPLICATE_NIT,
                        DomainErrorMessages.DUPLICATE_NIT
                ))
                        : Mono.empty());
    }

    public Mono<UserSummary> findOwnerByIdOrFail(Long ownerId, String token) {
        return userWebClientPort.findById(ownerId, token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.OWNER_NOT_FOUND,
                        DomainErrorMessages.OWNER_NOT_FOUND
                )));
    }

    public Mono<Void> validateOwnerIsActive(UserSummary owner) {
        return Boolean.TRUE.equals(owner.status())
                ? Mono.empty()
                : Mono.error(new DomainException(
                DomainErrorCode.OWNER_NOT_FOUND,
                DomainErrorMessages.OWNER_NOT_FOUND
        ));
    }

    public Mono<Void> validateOwnerHasOwnerRole(UserSummary owner) {
        return RoleNames.OWNER.equals(owner.roleName())
                ? Mono.empty()
                : Mono.error(new DomainException(
                DomainErrorCode.INVALID_OWNER_ROLE,
                DomainErrorMessages.INVALID_OWNER_ROLE
        ));
    }
}
