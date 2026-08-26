package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.builder.OrderBuilder;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.validation.order.ListOrdersDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRetrieveValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class ListOrdersUseCase implements IListOrdersServicePort {

    private final IOrderPersistencePort iOrderPersistencePort;
    private final OrderRetrieveValidator orderRetrieveValidator;
    private final ListOrdersDomainValidator listOrdersDomainValidator;

    @Override
    public Mono<PageResult<OrderQueryModel>> list(Long employeeId, String status, int page, int size) {
        return Mono.defer(() -> {
            listOrdersDomainValidator.validate(status, page, size);

            return orderRetrieveValidator.validateEmployeeHasRestaurantAssigned(employeeId)
                    .flatMap(restaurantId ->
                            Mono.zip(
                                    iOrderPersistencePort.countOrdersByRestaurantIdAndStatus(restaurantId, status),
                                    iOrderPersistencePort.findOrderIdsByRestaurantIdAndStatus(restaurantId, status, page, size)
                                            .collectList()
                                            .flatMap(orderIds -> {
                                                if (orderIds.isEmpty()) {
                                                    return Mono.just(List.<OrderQueryModel>of());
                                                }

                                                return iOrderPersistencePort.findOrdersDetailByIds(orderIds)
                                                        .collectList()
                                                        .map(OrderBuilder::buildOrderResponses);
                                            })
                            ).map(tuple -> {
                                long totalElements = tuple.getT1();
                                List<OrderQueryModel> listQueryModels = tuple.getT2();
                                int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                                return PageResult.<OrderQueryModel>builder()
                                        .content(listQueryModels)
                                        .page(page)
                                        .size(size)
                                        .totalElements(totalElements)
                                        .totalPages(totalPages)
                                        .build();
                            })
                    );
        });
    }
}
