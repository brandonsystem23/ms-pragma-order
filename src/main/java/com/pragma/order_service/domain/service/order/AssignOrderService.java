package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.port.in.AssignOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.AssignOrderValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class AssignOrderService implements AssignOrderUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final AssignOrderValidator assignOrderValidator;
    private final AssignOrderDomainValidator assignOrderDomainValidator;

    @Override
    public Mono<Order> assign(Long orderId, String token) {
        return Mono.defer(() -> {
            assignOrderDomainValidator.validate(orderId);

            return assignOrderValidator.validate(orderId, token)
                    .flatMap(order -> {
                        order.setStatus(OrderStatus.IN_PREPARATION);
                        return orderPersistencePort.save(order);
                    });
        });
    }
}
