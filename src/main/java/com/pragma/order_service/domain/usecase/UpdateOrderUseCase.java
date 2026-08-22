package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderPinValidator;
import com.pragma.order_service.domain.validation.order.OrderStatusUpdateValidator;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RequiredArgsConstructor
public class UpdateOrderUseCase implements IUpdateOrderServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final IRestaurantPersistencePort iRestaurantPersistencePort;
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
                .then(iUserWebClientPort.findById(order.getCustomerId(), token)
                        .map(UserSummary::phone)
                        .flatMap(phone -> iNotificationWebClientPort.sendReadyNotification(phone, token))
                        .then(Mono.defer(() -> {
                            order.setStatus(OrderStatus.READY);
                            return saveAndTrace(order, session, "Pedido listo para entregar", token);
                        })));
    }

    private Mono<Long> deliverOrder(Order order, AuthSession session, String pin, String token) {
        return orderStatusUpdateValidator.validateDeliver(order, session)
                .then(orderPinValidator.validateDeliveryPin(session, pin))
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
                        buildTraceability(savedOrder, session, description, token)
                                .flatMap(traceability -> iTraceabilityWebClientPort.create(traceability, token))
                                .thenReturn(savedOrder.getId())
                );
    }

    private Mono<Traceability> buildTraceability(Order order, AuthSession session, String description, String token) {
        return Mono.zip(
                iUserWebClientPort.findById(order.getCustomerId(), token),
                iRestaurantPersistencePort.findById(order.getRestaurantId())
                        .switchIfEmpty(Mono.error(new DomainException(
                                DomainErrorCode.RESTAURANT_NOT_FOUND,
                                DomainErrorMessages.RESTAURANT_NOT_FOUND
                        )))
        ).map(tuple -> {
            UserSummary customer = tuple.getT1();
            Restaurant restaurant = tuple.getT2();

            Long employeeAssignedId = OrderStatus.CANCELLED.equals(order.getStatus()) ? null : order.getEmployeeAssignedId();
            String employeeAssignedName = OrderStatus.CANCELLED.equals(order.getStatus()) ? null : session.fullName();

            return Traceability.builder()
                    .orderId(order.getId())
                    .customerId(order.getCustomerId())
                    .customerName(customer.firstName().concat(" ").concat(customer.lastName()))
                    .restaurantId(order.getRestaurantId())
                    .restaurantName(restaurant.getName())
                    .ownerRestaurant(restaurant.getOwnerId())
                    .employeeAssignedId(employeeAssignedId)
                    .employeeAssignedName(employeeAssignedName)
                    .status(order.getStatus())
                    .description(description)
                    .changedByUserId(session.userId())
                    .changedByRole(session.role())
                    .changedAt(LocalDateTime.now(ZoneId.of("America/Lima")))
                    .build();
        });
    }
}
