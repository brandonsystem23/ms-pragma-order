package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.builder.OrderBuilder;
import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
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
    public Mono<Long> update(Long orderId, UpdateOrderCommand updateOrderCommand, Long userId, String role,
                             String fullName, String numberDocument, String token) {
        return Mono.defer(() -> {
            updateOrderStatusDomainValidator.validate(orderId, updateOrderCommand);

            return findOrderByIdOrFail(orderId)
                    .flatMap(order -> processStatusUpdate(
                            order,
                            updateOrderCommand,
                            userId,
                            role,
                            fullName,
                            numberDocument,
                            token
                    ));
        });
    }

    private Mono<Long> processStatusUpdate(
            Order order,
            UpdateOrderCommand command,
            Long userId,
            String role,
            String fullName,
            String numberDocument,
            String token
    ) {
        return switch (command.status()) {
            case OrderStatus.IN_PREPARATION -> assignOrder(order, userId, role, fullName, token);
            case OrderStatus.READY -> markOrderReady(order, userId, role, fullName, token);
            case OrderStatus.DELIVERED -> deliverOrder(order, userId, role, fullName, numberDocument, command.pin(), token);
            case OrderStatus.CANCELLED -> cancelOrder(order, userId, role, fullName, token);
            default -> Mono.error(new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_UPDATE_NOT_SUPPORTED
            ));
        };
    }

    private Mono<Long> assignOrder(Order order, Long userId, String role, String fullName, String token) {
        return orderStatusUpdateValidator.validateEmployeeCanAssignOrder(userId, role, order)
                .then(Mono.defer(() ->
                        orderStatusUpdateValidator.findRestaurantIdByEmployeeOrFail(userId)
                ))
                .then(Mono.defer(() -> {
                    order.setEmployeeAssignedId(userId);
                    order.setStatus(OrderStatus.IN_PREPARATION);
                    return saveAndTrace(order, userId, role, fullName, "Pedido en preparación", token);
                }));
    }

    private Mono<Long> markOrderReady(Order order, Long userId, String role, String fullName, String token) {
        return orderStatusUpdateValidator.validateEmployeeCanMarkOrderReady(userId, role, order)
                .then(Mono.defer(() -> iUserWebClientPort.findById(order.getCustomerId(), token)
                        .map(UserSummary::phone)
                        .flatMap(phone -> iNotificationWebClientPort.sendReadyNotification(phone, token))
                        .then(Mono.defer(() -> {
                            order.setStatus(OrderStatus.READY);
                            return saveAndTrace(order, userId, role, fullName, "Pedido listo para entregar", token);
                        }))));
    }

    private Mono<Long> deliverOrder(Order order, Long userId, String role, String fullName,
                                    String numberDocument, String pin, String token) {
        return orderStatusUpdateValidator.validateEmployeeCanDeliverOrder(userId, role, order)
                .then(Mono.defer(() -> orderPinValidator.validateDeliveryPin(numberDocument, pin)))
                .then(Mono.defer(() -> {
                    order.setStatus(OrderStatus.DELIVERED);
                    return saveAndTrace(order, userId, role, fullName, "Pedido entregado", token);
                }));
    }

    private Mono<Long> cancelOrder(Order order, Long userId, String role, String fullName, String token) {
        return orderStatusUpdateValidator.validateClientCanCancelOrder(userId, role, order)
                .then(Mono.defer(() -> {
                    order.setStatus(OrderStatus.CANCELLED);
                    return saveAndTrace(order, userId, role, fullName, "Pedido cancelado", token);
                }));
    }

    private Mono<Order> findOrderByIdOrFail(Long orderId) {
        return iOrderPersistencePort.findById(orderId)
                .switchIfEmpty(Mono.error(new DomainException(
                        DomainErrorCode.ORDER_NOT_FOUND,
                        DomainErrorMessages.ORDER_NOT_FOUND
                )));
    }

    private Mono<Long> saveAndTrace(Order order, Long userId, String role, String fullName,
                                    String description, String token) {
        return iOrderPersistencePort.save(order)
                .flatMap(savedOrder ->
                        buildAndSendTraceability(savedOrder, userId, role, fullName, description, token)
                                .thenReturn(savedOrder.getId())
                );
    }

    private Mono<Void> buildAndSendTraceability(Order order, Long userId, String role, String fullName,
                                                String description, String token) {
        return iOrderPersistencePort.findOrderDetailById(order.getId())
                .collectList()
                .flatMap(orderDetails -> {
                    OrderDetail latestDetail = orderDetails.stream()
                            .max(Comparator.comparing(OrderDetail::getUpdatedAt))
                            .orElseThrow();

                    return sendTraceability(latestDetail, userId, role, fullName, description, token);
                });
    }

    private Mono<Void> sendTraceability(OrderDetail detail, Long userId, String role, String fullName,
                                        String description, String token) {
        Long employeeAssignedId = null;
        String employeeAssignedName = null;

        if (RoleNames.EMPLOYEE.equalsIgnoreCase(role)) {
            employeeAssignedId = userId;
            employeeAssignedName = fullName;
        }

        Traceability traceability = OrderBuilder.buildTraceability(
                detail,
                userId,
                role,
                employeeAssignedId,
                employeeAssignedName,
                description
        );

        return iTraceabilityWebClientPort.create(traceability, token).then();
    }
}
