package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.builder.OrderBuilder;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.RoleNames;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import java.util.List;


@RequiredArgsConstructor
public class CreateOrderUseCase implements ICreateOrderServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final IDishPersistencePort iDishPersistencePort;
    private final ITraceabilityWebClientPort iTraceabilityWebClientPort;
    private final OrderRegistrationValidator orderRegistrationValidator;
    private final OrderDomainValidator orderDomainValidator;

    @Override
    public Mono<OrderQueryModel> create(CreateOrderCommand createOrderCommand, String token) {
        return Mono.defer(() -> {

            orderDomainValidator.validateForCreate(createOrderCommand);

            return orderRegistrationValidator.validate(
                            createOrderCommand.restaurantId(),
                            createOrderCommand.items(),
                            token
                    )
                    .flatMap(customerId -> buildOrder(customerId, createOrderCommand))
                    .flatMap(iOrderPersistencePort::save)
                    .flatMap(savedOrder ->
                            iOrderPersistencePort.findOrderDetailById(savedOrder.getId())
                                    .collectList()
                                    .flatMap(orderDetails ->
                                            sendTraceability(orderDetails, token)
                                                    .thenReturn(OrderBuilder
                                                            .buildOrderQueryModel(orderDetails)
                                                    )
                                    )
                    );
        });
    }

    private Mono<Order> buildOrder(Long customerId, CreateOrderCommand createOrderCommand) {

        List<Long> dishIds = createOrderCommand.items().stream()
                .map(CreateOrderItemCommand::dishId)
                .toList();

        return iDishPersistencePort.findByIds(dishIds)
                .collectList()
                .map(dishes ->
                        OrderBuilder.buildOrder(
                                createOrderCommand,
                                dishes,
                                customerId)
                );
    }


    private Mono<Void> sendTraceability(List<OrderDetail> orderDetails, String token) {

        OrderDetail detail = orderDetails.getFirst();

        Traceability traceability = OrderBuilder.buildTraceability(
                detail,
                detail.getCustomerId(),
                RoleNames.CLIENT,
                "Pedido creado");

        return iTraceabilityWebClientPort.create(traceability, token)
                .then();
    }
}