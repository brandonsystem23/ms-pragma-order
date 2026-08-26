package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderStatusUpdateValidator {

    private final IRestaurantPersistencePort restaurantPersistencePort;

    public Mono<Void> validateEmployeeCanAssignOrder(Long employeeId, String role, Order order) {
        if (!RoleNames.EMPLOYEE.equals(role)) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_ASSIGN_ACCESS_DENIED
            ));
        }

        return findRestaurantIdByEmployeeOrFail(employeeId)
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

    public Mono<Void> validateEmployeeCanMarkOrderReady(Long employeeId, String role, Order order) {
        if (!RoleNames.EMPLOYEE.equals(role)) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_READY_ACCESS_DENIED
            ));
        }

        if (!OrderStatus.IN_PREPARATION.equals(order.getStatus())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_READY_INVALID_STATUS
            ));
        }

        if (order.getEmployeeAssignedId() == null || !employeeId.equals(order.getEmployeeAssignedId())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_READY_NOT_ASSIGNED_EMPLOYEE
            ));
        }

        return Mono.empty();
    }

    public Mono<Void> validateEmployeeCanDeliverOrder(Long employeeId, String role, Order order) {
        if (!RoleNames.EMPLOYEE.equals(role)) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_DELIVER_ACCESS_DENIED
            ));
        }

        if (!OrderStatus.READY.equals(order.getStatus())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_DELIVER_INVALID_STATUS
            ));
        }

        if (order.getEmployeeAssignedId() == null || !employeeId.equals(order.getEmployeeAssignedId())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_DELIVER_NOT_ASSIGNED_EMPLOYEE
            ));
        }

        return Mono.empty();
    }

    public Mono<Void> validateClientCanCancelOrder(Long customerId, String role, Order order) {
        if (!RoleNames.CLIENT.equals(role)) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_CANCEL_ACCESS_DENIED
            ));
        }

        if (!customerId.equals(order.getCustomerId())) {
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
    }

    public Mono<Long> findRestaurantIdByEmployeeOrFail(Long employeeId) {
        return restaurantPersistencePort.findRestaurantIdByEmployeeId(employeeId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.EMPLOYEE_RESTAURANT_NOT_FOUND,
                        DomainErrorMessages.EMPLOYEE_RESTAURANT_NOT_FOUND
                )));
    }
}
