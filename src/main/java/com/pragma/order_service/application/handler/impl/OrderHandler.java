package com.pragma.order_service.application.handler.impl;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.UpdateOrderResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IOrderHandler;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class OrderHandler implements IOrderHandler {

    private static final String MESSAGE = "Estado del pedido actualizado exitosamente";

    private final ICreateOrderServicePort iCreateOrderServicePort;
    private final IListOrdersServicePort iListOrdersServicePort;
    private final IUpdateOrderServicePort iUpdateOrderServicePort;
    private final OrderDtoMapper orderDtoMapper;

    @Override
    public Mono<UpdateOrderResponse> updateStatus(Long orderId, UpdateOrderRequest request, String token) {
        return iUpdateOrderServicePort.update(
                        orderId,
                        orderDtoMapper.toUpdateStatusCommand(request),
                        token
                )
                .map(updatedOrderId -> UpdateOrderResponse.builder()
                        .id(updatedOrderId)
                        .message(MESSAGE)
                        .build());
    }

    @Override
    public Mono<OrderResponse> create(CreateOrderRequest request, String token) {
        return iCreateOrderServicePort.create(orderDtoMapper.toCommand(request), token)
                .map(orderDtoMapper::toResponse);
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

}
