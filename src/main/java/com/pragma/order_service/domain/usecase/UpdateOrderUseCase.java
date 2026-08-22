package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateOrderUseCase implements IUpdateOrderServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final IRedisCachePort iRedisCachePort;
    private final IRestaurantPersistencePort iRestaurantPersistencePort;
    private final IUserWebClientPort iUserWebClientPort;
    private final INotificationWebClientPort iNotificationWebClientPort;
    private final UpdateOrderDomainValidator updateOrderStatusDomainValidator;

    @Override
    public Mono<Long> update(Long orderId, com.pragma.order_service.domain.model.command.UpdateOrderCommand updateOrderCommand, String token) {
        return Mono.defer(() -> {

            updateOrderStatusDomainValidator.validate(orderId, updateOrderCommand);

            return getSession(token)
                    .flatMap(session -> findOrder(orderId)
                            .flatMap(order -> processStatusUpdate(order, updateOrderCommand, session, token)));
        });
    }

    private Mono<Long> processStatusUpdate(
            Order order,
            com.pragma.order_service.domain.model.command.UpdateOrderCommand command,
            AuthSession session,
            String token
    ) {
        return switch (command.status()) {
            case OrderStatus.IN_PREPARATION -> assignOrder(order, session);
            case OrderStatus.READY -> markOrderReady(order, session, token);
            case OrderStatus.DELIVERED -> deliverOrder(order, session, command.pin());
            case OrderStatus.CANCELLED -> cancelOrder(order, session);
            default -> Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_UPDATE_NOT_SUPPORTED
            ));
        };
    }

    private Mono<Long> assignOrder(Order order, AuthSession session) {
        return validateEmployeeRole(session, DomainErrorMessages.ORDER_ASSIGN_ACCESS_DENIED)
                .then(findRestaurantIdByEmployee(session.userId()))
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

                    order.setEmployeeAssignedId(session.userId());
                    order.setStatus(OrderStatus.IN_PREPARATION);

                    return saveAndReturnId(order);
                });
    }

    private Mono<Long> markOrderReady(Order order, AuthSession session, String token) {
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

                    return iUserWebClientPort.findById(order.getCustomerId(), token)
                            .map(UserSummary::phone)
                            .flatMap(phone -> iNotificationWebClientPort.sendReadyNotification(phone, token))
                            .then(Mono.defer(() -> {
                                order.setStatus(OrderStatus.READY);
                                return saveAndReturnId(order);
                            }));
                }));
    }

    private Mono<Long> deliverOrder(Order order, AuthSession session, String pin) {
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

                    return iRedisCachePort.existsByEmployeeDocumentAndPin(session.numberDocument(), pin)
                            .flatMap(existsPin -> {
                                if (Boolean.FALSE.equals(existsPin)) {
                                    return Mono.error(new DomainException(
                                            DomainErrorCode.INVALID_PIN,
                                            DomainErrorMessages.ORDER_DELIVER_INVALID_PIN
                                    ));
                                }

                                order.setStatus(OrderStatus.DELIVERED);
                                return saveAndReturnId(order);
                            });
                }));
    }

    private Mono<Long> cancelOrder(Order order, AuthSession session) {
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

                    order.setStatus(OrderStatus.CANCELLED);
                    return saveAndReturnId(order);
                }));
    }

    private Mono<AuthSession> getSession(String token) {
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

    private Mono<Order> findOrder(Long orderId) {
        return iOrderPersistencePort.findById(orderId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.ORDER_NOT_FOUND,
                        DomainErrorMessages.ORDER_NOT_FOUND
                )));
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

    private Mono<Long> saveAndReturnId(Order order) {
        return iOrderPersistencePort.save(order)
                .map(Order::getId);
    }
}
