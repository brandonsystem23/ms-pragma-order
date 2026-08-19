package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AssignOrderValidator {

    private final AuthSessionPort authSessionPort;
    private final RestaurantPersistencePort restaurantPersistencePort;
    private final OrderPersistencePort orderPersistencePort;

    public Mono<Order> validate(Long orderId, String token) {
        return authSessionPort.findByToken(token)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.INVALID_TOKEN,
                        DomainErrorMessages.INVALID_TOKEN
                )))
                .flatMap(this::checkEmployeeRole)
                .flatMap(authSession ->
                        restaurantPersistencePort.findRestaurantIdByEmployeeId(authSession.userId())
                                .switchIfEmpty(Mono.error(new DomainException(
                                        DomainErrorCode.EMPLOYEE_RESTAURANT_NOT_FOUND,
                                        DomainErrorMessages.EMPLOYEE_RESTAURANT_NOT_FOUND
                                )))
                                .flatMap(restaurantId ->
                                        orderPersistencePort.findById(orderId)
                                                .switchIfEmpty(Mono.error(new DomainException(
                                                        DomainErrorCode.ORDER_NOT_FOUND,
                                                        DomainErrorMessages.ORDER_NOT_FOUND
                                                )))
                                                .flatMap(order -> validateOrder(order, restaurantId, authSession.userId()))
                                )
                );
    }

    private Mono<AuthSession> checkEmployeeRole(AuthSession authSession) {
        if (!RoleNames.EMPLOYEE.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_ASSIGN_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }

    private Mono<Order> validateOrder(Order order, Long restaurantId, Long employeeId) {
        if (!restaurantId.equals(order.getRestaurantId())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_ASSIGN_DIFFERENT_RESTAURANT
            ));
        }

        if (!OrderStatus.PENDING.equals(order.getStatus())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ASSIGN_INVALID_STATUS
            ));
        }

        if (order.getEmployeeAssignedId() != null) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ALREADY_ASSIGNED
            ));
        }

        order.setEmployeeAssignedId(employeeId);
        return Mono.just(order);
    }
}
