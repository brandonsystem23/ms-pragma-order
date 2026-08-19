package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.port.in.CreateOrderUseCase;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.service.order.validation.OrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.OrderRegistrationValidator;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final OrderRegistrationValidator orderRegistrationValidator;
    private final OrderDomainValidator orderDomainValidator;

    @Override
    public
    Mono<List<OrderSummary>> create(CreateOrderCommand command, String token) {
        return Mono.defer(() -> {
            orderDomainValidator.validateForCreate(command);

            return orderRegistrationValidator.validate(command.restaurantId(), command.items(), token)
                    .flatMap(customerId -> {
                        Order order = Order.builder()
                                .customerId(customerId)
                                .restaurantId(command.restaurantId())
                                .status(OrderStatus.PENDING)
                                .items(command.items().stream()
                                        .map(item -> OrderItem.builder()
                                                .dishId(item.dishId())
                                                .quantity(item.quantity())
                                                .build())
                                        .toList())
                                .build();

                        return orderPersistencePort.save(order);
                    })
                    .flatMap(savedOrder ->
                            orderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                    );
        });
    }
}
