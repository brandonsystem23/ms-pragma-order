package com.pragma.order_service.domain.builder;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderItemQueryModel;
import com.pragma.order_service.domain.model.query.OrderQueryModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OrderBuilder {

    private OrderBuilder() {

    }

    public static OrderQueryModel buildOrderQueryModel(List<OrderDetail> orderDetails) {
        OrderDetail order = orderDetails.getFirst();

        return OrderQueryModel.builder()
                .id(order.getOrderId())
                .customerId(order.getCustomerId())
                .nameCustomer(order.getCustomerName())
                .restaurantId(order.getRestaurantId())
                .nameRestaurant(order.getRestaurantName())
                .status(order.getStatus())
                .employeeAssignedId(order.getEmployeeAssignedId())
                .totalPrice(order.getTotalPrice())
                .items(buildListOrderItemQuery(orderDetails))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static List<OrderItemQueryModel> buildListOrderItemQuery(List<OrderDetail> orderDetails) {
        return orderDetails.stream()
                .map(item ->
                        OrderItemQueryModel.builder()
                                .dishId(item.getDishId())
                                .name(item.getDishName())
                                .quantity(item.getQuantity())
                                .price(item.getDishPrice())
                                .build()
                )
                .toList();
    }

    public static Traceability buildTraceability(OrderDetail detail, Long changedByUserId, String changedByRole,
                                                 Long employeeAssignedId, String employeeAssignedName,
                                                 String description) {
        return Traceability.builder()
                .orderId(detail.getOrderId())
                .customerId(detail.getCustomerId())
                .customerName(detail.getCustomerName())
                .restaurantId(detail.getRestaurantId())
                .restaurantName(detail.getRestaurantName())
                .ownerRestaurant(detail.getOwnerId())
                .employeeAssignedId(employeeAssignedId)
                .employeeAssignedName(employeeAssignedName)
                .status(detail.getStatus())
                .description(description)
                .changedByUserId(changedByUserId)
                .changedByRole(changedByRole)
                .changedAt(
                        LocalDateTime.now(
                                ZoneId.of("America/Lima")
                        )
                )
                .build();
    }

    public static Order buildOrder(CreateOrderCommand createOrderCommand, List<Dish> dishes, Long customerId) {
        Map<Long, Dish> dishesById = dishes.stream()
                .collect(Collectors.toMap(
                        Dish::getId,
                        Function.identity()
                ));

        List<OrderItem> items = buildListOrderItem(createOrderCommand, dishesById);

        BigDecimal totalPrice = calculateTotalPrice(items);

        return Order.builder()
                .customerId(customerId)
                .restaurantId(createOrderCommand.restaurantId())
                .status(OrderStatus.PENDING)
                .totalPrice(totalPrice)
                .items(items)
                .build();
    }

    public static List<OrderItem> buildListOrderItem(CreateOrderCommand createOrderCommand,
                                                     Map<Long, Dish> dishesById) {
        return createOrderCommand.items().stream()
                .map(itemCommand -> {
                    Dish dish = dishesById.get(itemCommand.dishId());
                    return OrderItem.builder()
                            .dishId(itemCommand.dishId())
                            .quantity(itemCommand.quantity())
                            .price(dish.getPrice())
                            .build();
                })
                .toList();
    }

    public static BigDecimal calculateTotalPrice(List<OrderItem> items) {
        return items.stream()
                .map(item ->
                        item.getPrice()
                                .multiply(item.getQuantity())
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static List<OrderQueryModel> buildOrderResponses(List<OrderDetail> summaries) {

        Map<Long, List<OrderDetail>> grouped = new LinkedHashMap<>();

        for (OrderDetail summary : summaries) {
            grouped.computeIfAbsent(summary.getOrderId(), key -> new ArrayList<>())
                    .add(summary);
        }

        return grouped.values().stream()
                .map(OrderBuilder::buildOrderQueryModel)
                .toList();
    }


}
