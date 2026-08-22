package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderItemQueryModel;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.validation.order.ListOrdersDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRetrieveValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ListOrdersUseCase implements IListOrdersServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final OrderRetrieveValidator orderRetrieveValidator;
    private final ListOrdersDomainValidator listOrdersDomainValidator;

    @Override
    public Mono<PageResult<OrderQueryModel>> list(String token, String status, int page, int size) {
        return Mono.defer(() -> {

            listOrdersDomainValidator.validate(status, page, size);

            return orderRetrieveValidator.validate(token)
                    .flatMap(restaurantId ->
                            Mono.zip(
                                    iOrderPersistencePort.countOrdersByRestaurantIdAndStatus(restaurantId, status),
                                    iOrderPersistencePort.findOrderIdsByRestaurantIdAndStatus(restaurantId, status,
                                                    page, size)
                                            .collectList()
                                            .flatMap(orderIds -> {
                                                if (orderIds.isEmpty()) {
                                                    return Mono.just(List.<OrderQueryModel>of());
                                                }

                                                return iOrderPersistencePort.findOrdersDetailByIds(orderIds)
                                                        .collectList()
                                                        .map(this::mapToOrderResponses);
                                            })
                            ).map(tuple -> {
                                long totalElements = tuple.getT1();
                                List<OrderQueryModel> content = tuple.getT2();
                                int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                                return PageResult.<OrderQueryModel>builder()
                                        .content(content)
                                        .page(page)
                                        .size(size)
                                        .totalElements(totalElements)
                                        .totalPages(totalPages)
                                        .build();
                            })
                    );
        });
    }

    private List<OrderQueryModel> mapToOrderResponses(List<OrderDetail> summaries) {

        Map<Long, List<OrderDetail>> grouped = new LinkedHashMap<>();

        for (OrderDetail summary : summaries) {
            grouped.computeIfAbsent(summary.getOrderId(), key -> new ArrayList<>())
                    .add(summary);
        }

        return grouped.values().stream()
                .map(this::buildToOrderQueryModel)
                .toList();
    }

    private OrderQueryModel buildToOrderQueryModel(List<OrderDetail> orderDetails) {

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
                .items(orderDetails.stream()
                        .map(item -> OrderItemQueryModel.builder()
                                .dishId(item.getDishId())
                                .name(item.getDishName())
                                .quantity(item.getQuantity())
                                .price(item.getDishPrice())
                                .build())
                        .toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
