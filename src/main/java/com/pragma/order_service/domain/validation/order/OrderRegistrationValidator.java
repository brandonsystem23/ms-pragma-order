package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class OrderRegistrationValidator {

    private final IRedisCachePort authSessionPort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final IOrderPersistencePort orderPersistencePort;

    public Mono<Long> validate(Long restaurantId, List<CreateOrderItemCommand> items, String token) {
        return validateClientRole(token)
                .flatMap(authSession ->
                        validateCustomerWithoutActiveOrder(authSession.userId(), restaurantId)
                                .then(Mono.defer(() -> validateRestaurantExists(restaurantId)))
                                .then(Mono.defer(() -> validateDishes(items, restaurantId)))
                                .thenReturn(authSession.userId())
                );
    }

    private Mono<AuthSession> validateClientRole(String token) {
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
                .flatMap(this::checkClientRole);
    }

    private Mono<AuthSession> checkClientRole(AuthSession authSession) {
        if (!RoleNames.CLIENT.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_CREATE_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }

    private Mono<Void> validateCustomerWithoutActiveOrder(Long customerId, Long restaurantId) {
        return orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(customerId, restaurantId)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DomainException(
                        DomainErrorCode.ACTIVE_ORDER_EXISTS,
                        DomainErrorMessages.ACTIVE_ORDER_EXISTS
                ))
                        : Mono.empty());
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

    private Mono<Void> validateDishes(List<CreateOrderItemCommand> items, Long restaurantId) {
        return Flux.fromIterable(items)
                .flatMap(item -> validateDish(item.dishId(), restaurantId))
                .then();
    }

    private Mono<Void> validateDish(Long dishId, Long restaurantId) {
        return dishPersistencePort.findByIdAndStatusTrue(dishId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.DISH_NOT_FOUND,
                        DomainErrorMessages.DISH_NOT_FOUND
                )))
                .flatMap(dish -> validateDishBelongsToRestaurant(dish, restaurantId));
    }

    private Mono<Void> validateDishBelongsToRestaurant(Dish dish, Long restaurantId) {
        if (!restaurantId.equals(dish.getRestaurantId())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.INVALID_ORDER_RESTAURANT,
                    DomainErrorMessages.ORDER_DISH_INVALID_RESTAURANT
            ));
        }

        return Mono.empty();
    }
}
