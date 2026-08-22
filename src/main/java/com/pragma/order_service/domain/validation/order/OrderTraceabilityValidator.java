package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class OrderTraceabilityValidator {

    private final IRestaurantPersistencePort iRestaurantPersistencePort;

    public Mono<Restaurant> validateAndGetRestaurant(List<OrderDetail> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return Mono.empty();
        }

        OrderDetail detail = orderDetails.getFirst();

        return iRestaurantPersistencePort.findById(detail.getRestaurantId())
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.RESTAURANT_NOT_FOUND
                )));
    }
}
