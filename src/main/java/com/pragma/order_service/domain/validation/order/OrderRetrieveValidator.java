package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderRetrieveValidator {

    private final IRestaurantPersistencePort restaurantPersistencePort;

    public Mono<Long> validateEmployeeHasRestaurantAssigned(Long employeeId) {
        return restaurantPersistencePort.findRestaurantIdByEmployeeId(employeeId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.EMPLOYEE_RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.EMPLOYEE_RESTAURANT_NOT_FOUND
                )));
    }
}
