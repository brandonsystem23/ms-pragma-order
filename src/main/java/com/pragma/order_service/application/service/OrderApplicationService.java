package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.port.in.AssignOrderUseCase;
import com.pragma.order_service.domain.port.in.CreateOrderUseCase;
import com.pragma.order_service.domain.port.in.DeliverOrderUseCase;
import com.pragma.order_service.domain.port.in.ListOrdersUseCase;
import com.pragma.order_service.domain.port.in.MarkOrderReadyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final CreateOrderUseCase createOrderUseCase;
    private final AssignOrderUseCase assignOrderUseCase;
    private final OrderDtoMapper orderDtoMapper;
    private final ListOrdersUseCase listOrdersUseCase;
    private final MarkOrderReadyUseCase markOrderReadyUseCase;
    private final DeliverOrderUseCase deliverOrderUseCase;

    public Mono<OrderResponse> create(CreateOrderRequest request, String token) {
        return createOrderUseCase.create(orderDtoMapper.toCommand(request), token)
                .map(this::toOrderResponse);
    }

    public Mono<OrderResponse> assign(Long orderId, String token) {
        return assignOrderUseCase.assign(orderId, token)
                .map(this::toOrderResponse);
    }

    public Mono<OrderResponse> markReady(Long orderId, String token) {
        return markOrderReadyUseCase.markReady(orderId, token)
                .map(this::toOrderResponse);
    }

    public Mono<OrderResponse> deliver(Long orderId, String pin, String token) {
        return deliverOrderUseCase.deliver(orderId, pin, token)
                .map(this::toOrderResponse);
    }

    public Mono<PagedResponse<OrderResponse>> list(String token, String status, int page, int size) {
        return listOrdersUseCase.list(token, status, page, size)
                .map(result -> PagedResponse.<OrderResponse>builder()
                        .content(result.content().stream()
                                .map(orderDtoMapper::toResponse)
                                .toList())
                        .page(result.page())
                        .size(result.size())
                        .totalElements(result.totalElements())
                        .totalPages(result.totalPages())
                        .build());
    }

    private OrderResponse toOrderResponse(List<OrderDetail> orderDetails) {
        OrderDetail orderDetail = orderDetails.getFirst();
        List<OrderItemResponse> items = orderDetails.stream()
                .map(orderDtoMapper::toItemResponse)
                .toList();

        return orderDtoMapper.toResponse(orderDetail, items);
    }

}
