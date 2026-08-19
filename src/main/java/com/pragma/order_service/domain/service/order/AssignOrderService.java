package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.port.in.AssignOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.AssignOrderValidator;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class AssignOrderService implements AssignOrderUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final AssignOrderValidator assignOrderValidator;
    private final AssignOrderDomainValidator assignOrderDomainValidator;

    @Override
    public Mono<List<OrderSummary>> assign(Long orderId, String token) {
        return Mono.defer(() -> {
            assignOrderDomainValidator.validate(orderId);

            return assignOrderValidator.validate(orderId, token)
                    .flatMap(orderPersistencePort::save)
                    .flatMap(savedOrder ->
                            orderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                    );
        });
    }
}
