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
import com.pragma.order_service.domain.port.out.OrderPinValidationPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeliverOrderValidator {

    private final AuthSessionPort authSessionPort;
    private final OrderPersistencePort orderPersistencePort;
    private final OrderPinValidationPort orderPinValidationPort;

    public Mono<Order> validate(Long orderId, String pin, String token) {
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
                .flatMap(this::checkEmployeeRole)
                .flatMap(authSession ->
                        orderPersistencePort.findById(orderId)
                                .switchIfEmpty(Mono.error(new DomainException(
                                        DomainErrorCode.ORDER_NOT_FOUND,
                                        DomainErrorMessages.ORDER_NOT_FOUND
                                )))
                                .flatMap(order -> validateOrder(order, authSession, pin))
                );
    }

    private Mono<AuthSession> checkEmployeeRole(AuthSession authSession) {
        if (!RoleNames.EMPLOYEE.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_DELIVER_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }

    private Mono<Order> validateOrder(Order order, AuthSession authSession, String pin) {
        if (!OrderStatus.READY.equals(order.getStatus())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_DELIVER_INVALID_STATUS
            ));
        }

        if (order.getEmployeeAssignedId() == null || !authSession.userId().equals(order.getEmployeeAssignedId())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_DELIVER_NOT_ASSIGNED_EMPLOYEE
            ));
        }

        return orderPinValidationPort.existsByEmployeeDocumentAndPin(authSession.numberDocument(), pin)
                .flatMap(existsPin -> {
                    if (Boolean.FALSE.equals(existsPin)) {
                        return Mono.error(new DomainException(
                                DomainErrorCode.INVALID_PIN,
                                DomainErrorMessages.ORDER_DELIVER_INVALID_PIN
                        ));
                    }

                    order.setStatus(OrderStatus.DELIVERED);
                    return Mono.just(order);
                });
    }
}
