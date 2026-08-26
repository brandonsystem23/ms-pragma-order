package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class OrderRegistrationValidator {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final IOrderPersistencePort orderPersistencePort;

    public Mono<Void> validateOrderCreationRules(Long restaurantId, List<CreateOrderItemCommand> items, Long customerId) {
        return validateRestaurantExists(restaurantId)
                .then(Mono.defer(() -> validateCustomerHasNoActiveOrder(customerId, restaurantId)))
                .then(Mono.defer(() -> validateAllDishesBelongToRestaurant(items, restaurantId)));
    }

    public Mono<Void> validateCustomerHasNoActiveOrder(Long customerId, Long restaurantId) {
        return orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(customerId, restaurantId)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new DomainException(
                        DomainErrorCode.ACTIVE_ORDER_EXISTS,
                        DomainErrorMessages.ACTIVE_ORDER_EXISTS
                ))
                        : Mono.empty());
    }

    public Mono<Void> validateRestaurantExists(Long restaurantId) {
        return restaurantPersistencePort.existById(restaurantId)
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                        DomainErrorCode.RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.RESTAURANT_NOT_FOUND
                )));
    }

    public Mono<Void> validateAllDishesBelongToRestaurant(List<CreateOrderItemCommand> items, Long restaurantId) {
        return Flux.fromIterable(items)
                .flatMap(item -> findActiveDishByIdOrFail(item.dishId())
                        .flatMap(dish -> validateDishBelongsToRestaurant(dish, restaurantId)))
                .then();
    }

    public Mono<Dish> findActiveDishByIdOrFail(Long dishId) {
        return dishPersistencePort.findByIdAndStatusTrue(dishId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.DISH_NOT_FOUND,
                        DomainErrorMessages.DISH_NOT_FOUND
                )));
    }

    public Mono<Void> validateDishBelongsToRestaurant(Dish dish, Long restaurantId) {
        if (!restaurantId.equals(dish.getRestaurantId())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.INVALID_ORDER_RESTAURANT,
                    DomainErrorMessages.ORDER_DISH_INVALID_RESTAURANT
            ));
        }

        return Mono.empty();
    }
}
