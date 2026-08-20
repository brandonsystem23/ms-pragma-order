package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.port.in.CancelOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.CancelOrderValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class CancelOrderService implements CancelOrderUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final CancelOrderValidator cancelOrderValidator;
    private final AssignOrderDomainValidator assignOrderDomainValidator;

    @Override
    public Mono<List<OrderDetail>> cancel(Long orderId, String token) {
        return Mono.defer(() -> {
            assignOrderDomainValidator.validate(orderId);

            return cancelOrderValidator.validate(orderId, token)
                    .flatMap(orderPersistencePort::save)
                    .flatMap(savedOrder ->
                            orderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                    );
        });
    }
}
