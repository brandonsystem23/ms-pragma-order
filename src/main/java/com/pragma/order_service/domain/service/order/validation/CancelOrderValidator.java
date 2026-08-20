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
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CancelOrderValidator {

    private final AuthSessionPort authSessionPort;
    private final OrderPersistencePort orderPersistencePort;

    public Mono<Order> validate(Long orderId, String token) {
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
                .flatMap(this::checkClientRole)
                .flatMap(authSession ->
                        orderPersistencePort.findById(orderId)
                                .switchIfEmpty(Mono.error(new DomainException(
                                        DomainErrorCode.ORDER_NOT_FOUND,
                                        DomainErrorMessages.ORDER_NOT_FOUND
                                )))
                                .flatMap(order -> validateOrder(order, authSession.userId()))
                );
    }

    private Mono<AuthSession> checkClientRole(AuthSession authSession) {
        if (!RoleNames.CLIENT.equals(authSession.role())) {
            return Mono.error(new DomainException(
                    DomainErrorCode.ACCESS_DENIED,
                    DomainErrorMessages.ORDER_CANCEL_ACCESS_DENIED
            ));
        }

        return Mono.just(authSession);
    }

    private Mono<Order> validateOrder(Order order, Long customerId) {
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

        order.setStatus(OrderStatus.CANCELLED);
        return Mono.just(order);
    }
}
