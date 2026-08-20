package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.port.in.DeliverOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.DeliverOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.DeliverOrderValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class DeliverOrderService implements DeliverOrderUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final DeliverOrderValidator deliverOrderValidator;
    private final DeliverOrderDomainValidator deliverOrderDomainValidator;

    @Override
    public Mono<List<OrderDetail>> deliver(Long orderId, String pin, String token) {
        return Mono.defer(() -> {
            deliverOrderDomainValidator.validate(orderId, pin);

            return deliverOrderValidator.validate(orderId, pin, token)
                    .flatMap(orderPersistencePort::save)
                    .flatMap(savedOrder ->
                            orderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                    );
        });
    }
}
