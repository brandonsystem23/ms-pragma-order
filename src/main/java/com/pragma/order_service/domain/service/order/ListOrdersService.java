package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.domain.port.in.ListOrdersUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.ListOrdersDomainValidator;
import com.pragma.order_service.domain.service.order.validation.OrderRetrieveValidator;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final OrderRetrieveValidator orderRetrieveValidator;
    private final ListOrdersDomainValidator listOrdersDomainValidator;

    @Override
    public Mono<PagedResponse<OrderResponse>> list(String token, String status, int page, int size) {
        return Mono.defer(() -> {
            listOrdersDomainValidator.validate(status, page, size);

            return orderRetrieveValidator.validate(token)
                    .flatMap(restaurantId ->
                            Mono.zip(
                                    orderPersistencePort.countOrdersByRestaurantIdAndStatus(restaurantId, status),
                                    orderPersistencePort.findOrderIdsByRestaurantIdAndStatus(restaurantId, status, page, size)
                                            .collectList()
                                            .flatMap(orderIds -> {
                                                if (orderIds.isEmpty()) {
                                                    return Mono.just(List.<OrderResponse>of());
                                                }

                                                return orderPersistencePort.findOrdersDetailByIds(orderIds)
                                                        .collectList()
                                                        .map(this::mapToOrderResponses);
                                            })
                            ).map(tuple -> {
                                long totalElements = tuple.getT1();
                                List<OrderResponse> content = tuple.getT2();
                                int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                                return PagedResponse.<OrderResponse>builder()
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

    private List<OrderResponse> mapToOrderResponses(List<OrderSummary> summaries) {
        Map<Long, List<OrderSummary>> grouped = new LinkedHashMap<>();

        for (OrderSummary summary : summaries) {
            grouped.computeIfAbsent(summary.getOrderId(), key -> new ArrayList<>())
                    .add(summary);
        }

        return grouped.values().stream()
                .map(ordersSummary -> {
                    OrderSummary order = ordersSummary.getFirst();

                    return OrderResponse.builder()
                            .id(order.getOrderId())
                            .customerId(order.getCustomerId())
                            .nameCustomer(order.getCustomerName())
                            .restaurantId(order.getRestaurantId())
                            .nameRestaurant(order.getRestaurantName())
                            .status(order.getStatus())
                            .items(ordersSummary.stream()
                                    .map(item -> OrderItemResponse.builder()
                                            .dishId(item.getDishId())
                                            .name(item.getDishName())
                                            .quantity(item.getQuantity())
                                            .build())
                                    .toList())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .build();
                })
                .toList();
    }
}
