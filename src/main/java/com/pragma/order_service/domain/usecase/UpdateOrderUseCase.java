package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.builder.OrderBuilder;
import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderPinValidator;
import com.pragma.order_service.domain.validation.order.OrderStatusUpdateValidator;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.util.Comparator;


@RequiredArgsConstructor
public class UpdateOrderUseCase implements IUpdateOrderServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final IUserWebClientPort iUserWebClientPort;
    private final INotificationWebClientPort iNotificationWebClientPort;
    private final ITraceabilityWebClientPort iTraceabilityWebClientPort;
    private final UpdateOrderDomainValidator updateOrderStatusDomainValidator;
    private final OrderStatusUpdateValidator orderStatusUpdateValidator;
    private final OrderPinValidator orderPinValidator;

    @Override
    public Mono<Long> update(Long orderId, UpdateOrderCommand updateOrderCommand, String token) {
        return Mono.defer(() -> {

            updateOrderStatusDomainValidator.validate(orderId, updateOrderCommand);

            return orderStatusUpdateValidator.getSession(token)
                    .flatMap(session -> findOrder(orderId)
                            .flatMap(order -> processStatusUpdate(order, updateOrderCommand, session, token)));
        });
    }

    private Mono<Long> processStatusUpdate(
            Order order,
            UpdateOrderCommand command,
            AuthSession session,
            String token
    ) {
        return switch (command.status()) {
            case OrderStatus.IN_PREPARATION -> assignOrder(order, session, token);
            case OrderStatus.READY -> markOrderReady(order, session, token);
            case OrderStatus.DELIVERED -> deliverOrder(order, session, command.pin(), token);
            case OrderStatus.CANCELLED -> cancelOrder(order, session, token);
            default -> Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_UPDATE_NOT_SUPPORTED
            ));
        };
    }

    private Mono<Long> assignOrder(Order order, AuthSession session, String token) {
        return orderStatusUpdateValidator.validateAssignOrder(order, session)
                .then(Mono.defer(() -> {
                    order.setEmployeeAssignedId(session.userId());
                    order.setStatus(OrderStatus.IN_PREPARATION);
                    return saveAndTrace(order, session, "Pedido en preparación", token);
                }));
    }

    private Mono<Long> markOrderReady(Order order, AuthSession session, String token) {
        return orderStatusUpdateValidator.validateMarkReady(order, session)
                .then(Mono.defer(() -> iUserWebClientPort.findById(order.getCustomerId(), token)
                        .map(UserSummary::phone)
                        .flatMap(phone -> iNotificationWebClientPort.sendReadyNotification(phone, token))
                        .then(Mono.defer(() -> {
                            order.setStatus(OrderStatus.READY);
                            return saveAndTrace(order, session, "Pedido listo para entregar", token);
                        }))));
    }

    private Mono<Long> deliverOrder(Order order, AuthSession session, String pin, String token) {
        return orderStatusUpdateValidator.validateDeliver(order, session)
                .then(Mono.defer(() -> orderPinValidator.validateDeliveryPin(session, pin)))
                .then(Mono.defer(() -> {
                    order.setStatus(OrderStatus.DELIVERED);
                    return saveAndTrace(order, session, "Pedido entregado", token);
                }));
    }

    private Mono<Long> cancelOrder(Order order, AuthSession session, String token) {
        return orderStatusUpdateValidator.validateCancel(order, session)
                .then(Mono.defer(() -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    return saveAndTrace(order, session, "Pedido cancelado", token);
                }));
    }

    private Mono<Order> findOrder(Long orderId) {
        return iOrderPersistencePort.findById(orderId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.ORDER_NOT_FOUND,
                        DomainErrorMessages.ORDER_NOT_FOUND
                )));
    }

    private Mono<Long> saveAndTrace(Order order, AuthSession session, String description, String token) {
        return iOrderPersistencePort.save(order)
                .flatMap(savedOrder ->
                        buildAndSendTraceability(savedOrder, session, description, token)
                                .thenReturn(savedOrder.getId())
                );
    }

    private Mono<Void> buildAndSendTraceability(Order order, AuthSession session, String description, String token) {

        return iOrderPersistencePort.findOrderDetailById(order.getId())
                .collectList()
                .flatMap(orderDetails -> {
                    OrderDetail latestDetail = orderDetails.stream()
                            .max(Comparator.comparing(OrderDetail::getUpdatedAt))
                            .orElseThrow();

                    return sendTraceability(latestDetail, description, token, session);
                });
    }

    private Mono<Void> sendTraceability(OrderDetail detail, String description, String token, AuthSession session) {
        Long employeeAssignedId = null;
        String employeeAssignedName = null;
        if (session.role().equalsIgnoreCase(RoleNames.EMPLOYEE)) {
            employeeAssignedId = session.userId();
            employeeAssignedName = session.fullName();
        }

        Traceability traceability = OrderBuilder.buildTraceability(
                detail,
                detail.getCustomerId(),
                session.role(),
                employeeAssignedId,
                employeeAssignedName,
                description
        );

        return iTraceabilityWebClientPort.create(traceability, token)
                .then();
    }
}
