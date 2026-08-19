package com.pragma.order_service.application.service;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.mapper.OrderDtoMapper;
import com.pragma.order_service.domain.port.in.CreateOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderDtoMapper orderDtoMapper;
    private final OrderPersistencePort orderPersistencePort;

    public Mono<OrderResponse> create(CreateOrderRequest request, String token) {
        return createOrderUseCase.create(orderDtoMapper.toCommand(request), token)
                .flatMap(savedOrder ->
                        orderPersistencePort.findOrderDetailById(savedOrder.getId())
                                .collectList()
                )
                .map(listOrderSummary -> {

                    OrderSummary orderSummary = listOrderSummary.getFirst();

                    List<OrderItemResponse> items = listOrderSummary.stream()
                            .map(orderDtoMapper::toItemResponse)
                            .toList();

                    return orderDtoMapper.toResponse(orderSummary, items);
                });
    }
}
