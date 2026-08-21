package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IOrderHandler;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderHandler implements IOrderHandler {

    private final ICreateOrderServicePort iCreateOrderServicePort;
    private final IListOrdersServicePort iListOrdersServicePort;
    private final IUpdateOrderServicePort iUpdateOrderServicePort;
    private final OrderDtoMapper orderDtoMapper;

    @Override
    public Mono<OrderResponse> updateStatus(Long orderId, UpdateOrderRequest request, String token) {
        return iUpdateOrderServicePort.update(
                        orderId,
                        orderDtoMapper.toUpdateStatusCommand(request),
                        token
                )
                .map(this::toOrderResponse);
    }

    @Override
    public Mono<OrderResponse> create(CreateOrderRequest request, String token) {
        return iCreateOrderServicePort.create(orderDtoMapper.toCommand(request), token)
                .map(this::toOrderResponse);
    }

    @Override
    public Mono<PagedResponse<OrderResponse>> list(String token, String status, int page, int size) {
        return iListOrdersServicePort.list(token, status, page, size)
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
