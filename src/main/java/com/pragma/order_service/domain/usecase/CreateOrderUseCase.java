package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import com.pragma.order_service.domain.validation.order.OrderTraceabilityValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CreateOrderUseCase implements ICreateOrderServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final IDishPersistencePort iDishPersistencePort;
    private final ITraceabilityWebClientPort iTraceabilityWebClientPort;
    private final OrderRegistrationValidator orderRegistrationValidator;
    private final OrderDomainValidator orderDomainValidator;
    private final OrderTraceabilityValidator orderTraceabilityValidator;

    @Override
    public Mono<List<OrderDetail>> create(CreateOrderCommand createOrderCommand, String token) {
        return Mono.defer(() -> {

            orderDomainValidator.validateForCreate(createOrderCommand);

            return orderRegistrationValidator.validate(
                            createOrderCommand.restaurantId(),
                            createOrderCommand.items(),
                            token
                    )
                    .flatMap(customerId -> buildOrder(customerId, createOrderCommand))
                    .flatMap(iOrderPersistencePort::save)
                    .flatMap(savedOrder ->
                            iOrderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                                    .flatMap(orderDetails ->
                                            orderTraceabilityValidator.validateAndGetRestaurant(orderDetails)
                                                    .flatMap(restaurant -> sendTraceability(orderDetails, restaurant, token))
                                                    .thenReturn(orderDetails)
                                    )
                    );
        });
    }

    private Mono<Order> buildOrder(Long customerId, CreateOrderCommand createOrderCommand) {
        List<Long> dishIds = createOrderCommand.items().stream()
                .map(CreateOrderItemCommand::dishId)
                .toList();

        return iDishPersistencePort.findByIds(dishIds)
                .collectList()
                .map(dishes -> {
                    Map<Long, Dish> dishesById = dishes.stream()
                            .collect(Collectors.toMap(Dish::getId, Function.identity()));

                    List<OrderItem> items = createOrderCommand.items().stream()
                            .map(itemCommand -> {
                                Dish dish = dishesById.get(itemCommand.dishId());
                                return OrderItem.builder()
                                        .dishId(itemCommand.dishId())
                                        .quantity(itemCommand.quantity())
                                        .price(dish.getPrice())
                                        .build();
                            })
                            .toList();

                    BigDecimal totalPrice = items.stream()
                            .map(item -> item.getPrice().multiply(item.getQuantity()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return Order.builder()
                            .customerId(customerId)
                            .restaurantId(createOrderCommand.restaurantId())
                            .status(OrderStatus.PENDING)
                            .totalPrice(totalPrice)
                            .items(items)
                            .build();
                });
    }

    private Mono<Void> sendTraceability(List<OrderDetail> orderDetails, Restaurant restaurant, String token) {

        OrderDetail detail = orderDetails.getFirst();

        return buildTraceability(detail, restaurant)
                .flatMap(traceability -> iTraceabilityWebClientPort.create(traceability, token))
                .then();
    }

    private Mono<Traceability> buildTraceability(OrderDetail detail, Restaurant restaurant) {
        return Mono.just(Traceability.builder()
                .orderId(detail.getOrderId())
                .customerId(detail.getCustomerId())
                .customerName(detail.getCustomerName())
                .restaurantId(detail.getRestaurantId())
                .restaurantName(detail.getRestaurantName())
                .ownerRestaurant(restaurant.getOwnerId())
                .employeeAssignedId(null)
                .employeeAssignedName(null)
                .status(detail.getStatus())
                .description("Pedido creado")
                .changedByUserId(detail.getCustomerId())
                .changedByRole(RoleNames.CLIENT)
                .changedAt(LocalDateTime.now(ZoneId.of("America/Lima")))
                .build());
    }
}
