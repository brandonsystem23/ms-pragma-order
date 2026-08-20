package com.pragma.order_service.domain.service.order;

import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.port.in.MarkOrderReadyUseCase;
import com.pragma.order_service.domain.port.out.NotificationWebClientPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.port.out.UserWebClientPort;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.MarkOrderReadyValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class MarkOrderReadyService implements MarkOrderReadyUseCase {

    private final OrderPersistencePort orderPersistencePort;
    private final MarkOrderReadyValidator markOrderReadyValidator;
    private final AssignOrderDomainValidator assignOrderDomainValidator;
    private final UserWebClientPort userWebClientPort;
    private final NotificationWebClientPort notificationWebClientPort;

    @Override
    public Mono<List<OrderDetail>> markReady(Long orderId, String token) {
        return Mono.defer(() -> {
            assignOrderDomainValidator.validate(orderId);

            return markOrderReadyValidator.validate(orderId, token)
                    .flatMap(order ->
                            orderPersistencePort.findOrderDetailById(order.getId())
                                    .collectList()
                                    .flatMap(orderDetails -> {
                                        OrderDetail orderDetail = orderDetails.getFirst();

                                        return userWebClientPort.findById(orderDetail.getCustomerId(), token)
                                                .map(userResponse -> UserSummary.builder()
                                                        .id(userResponse.id())
                                                        .firstName(userResponse.firstName())
                                                        .lastName(userResponse.lastName())
                                                        .email(userResponse.email())
                                                        .phone(userResponse.phone())
                                                        .status(userResponse.status())
                                                        .roleName(userResponse.roleName())
                                                        .build())
                                                .map(UserSummary::phone)
                                                .flatMap(phone -> notificationWebClientPort.sendReadyNotification(phone, token))
                                                .then(Mono.defer(() -> orderPersistencePort.save(order)))
                                                .flatMap(savedOrder ->
                                                        orderPersistencePort.findOrderDetailById(savedOrder.getId())
                                                                .collectList()
                                                );
                                    })
                    );
        });
    }
}
