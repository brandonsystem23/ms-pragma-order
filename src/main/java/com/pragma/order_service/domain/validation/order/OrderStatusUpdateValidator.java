package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderStatusUpdateValidator {

    private final IRedisCachePort iRedisCachePort;
    private final IRestaurantPersistencePort iRestaurantPersistencePort;

    public Mono<AuthSession> getSession(String token) {
        return iRedisCachePort.findByToken(token)
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
                        .build());
    }

    public Mono<Void> validateAssignOrder(Order order, AuthSession session) {
        return validateEmployeeRole(session, DomainErrorMessages.ORDER_ASSIGN_ACCESS_DENIED)
                .then(Mono.defer(() -> findRestaurantIdByEmployee(session.userId())))
                .flatMap(restaurantId -> {
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

                    return Mono.empty();
                });
    }

    public Mono<Void> validateMarkReady(Order order, AuthSession session) {
        return validateEmployeeRole(session, DomainErrorMessages.ORDER_READY_ACCESS_DENIED)
                .then(Mono.defer(() -> {
                    if (!OrderStatus.IN_PREPARATION.equals(order.getStatus())) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.VALIDATION_ERROR,
                                DomainErrorMessages.ORDER_READY_INVALID_STATUS
                        ));
                    }

                    if (order.getEmployeeAssignedId() == null || !session.userId().equals(order.getEmployeeAssignedId())) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.ACCESS_DENIED,
                                DomainErrorMessages.ORDER_READY_NOT_ASSIGNED_EMPLOYEE
                        ));
                    }

                    return Mono.empty();
                }));
    }

    public Mono<Void> validateDeliver(Order order, AuthSession session) {
        return validateEmployeeRole(session, DomainErrorMessages.ORDER_DELIVER_ACCESS_DENIED)
                .then(Mono.defer(() -> {
                    if (!OrderStatus.READY.equals(order.getStatus())) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.VALIDATION_ERROR,
                                DomainErrorMessages.ORDER_DELIVER_INVALID_STATUS
                        ));
                    }

                    if (order.getEmployeeAssignedId() == null || !session.userId().equals(order.getEmployeeAssignedId())) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.ACCESS_DENIED,
                                DomainErrorMessages.ORDER_DELIVER_NOT_ASSIGNED_EMPLOYEE
                        ));
                    }

                    return Mono.empty();
                }));
    }

    public Mono<Void> validateCancel(Order order, AuthSession session) {
        return validateClientRole(session, DomainErrorMessages.ORDER_CANCEL_ACCESS_DENIED)
                .then(Mono.defer(() -> {
                    if (!session.userId().equals(order.getCustomerId())) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.ACCESS_DENIED,
                                DomainErrorMessages.ORDER_CANCEL_NOT_CREATED
                        ));
                    }

                    if (!OrderStatus.PENDING.equals(order.getStatus())) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.VALIDATION_ERROR,
                                DomainErrorMessages.ORDER_CANCEL_INVALID_STATUS
                        ));
                    }

                    return Mono.empty();
                }));
    }

    private Mono<Long> findRestaurantIdByEmployee(Long employeeId) {
        return iRestaurantPersistencePort.findRestaurantIdByEmployeeId(employeeId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.EMPLOYEE_RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.EMPLOYEE_RESTAURANT_NOT_FOUND
                )));
    }

    private Mono<Void> validateEmployeeRole(AuthSession session, String message) {
        if (!RoleNames.EMPLOYEE.equals(session.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    message
            ));
        }

        return Mono.empty();
    }

    private Mono<Void> validateClientRole(AuthSession session, String message) {
        if (!RoleNames.CLIENT.equals(session.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    message
            ));
        }

        return Mono.empty();
    }
}
