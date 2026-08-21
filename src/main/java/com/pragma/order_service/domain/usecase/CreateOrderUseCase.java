package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class CreateOrderUseCase implements ICreateOrderServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final OrderRegistrationValidator orderRegistrationValidator;
    private final OrderDomainValidator orderDomainValidator;

    @Override
    public Mono<List<OrderDetail>> create(CreateOrderCommand createOrderCommand, String token) {
        return Mono.defer(() -> {

            orderDomainValidator.validateForCreate(createOrderCommand);

            return orderRegistrationValidator.validate(
                            createOrderCommand.restaurantId(),
                            createOrderCommand.items(),
                            token
                    )
                    .flatMap(customerId ->
                            iOrderPersistencePort.save(
                                    buildToOrder(customerId,  createOrderCommand)
                            )
                    )
                    .flatMap(savedOrder ->
                            iOrderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                    );
        });
    }

    private Order buildToOrder(Long customerId, CreateOrderCommand createOrderCommand) {
        return Order.builder()
                .customerId(customerId)
                .restaurantId(createOrderCommand.restaurantId())
                .status(OrderStatus.PENDING)
                .items(createOrderCommand.items().stream()
                        .map(item -> OrderItem.builder()
                                .dishId(item.dishId())
                                .quantity(item.quantity())
                                .build())
                        .toList())
                .build();
    }
}
