package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import com.pragma.order_service.domain.builder.RestaurantBuilder;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.validation.restaurant.ListRestaurantsDomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListRestaurantsUseCase implements IListRestaurantsServicePort {

    private final IRestaurantPersistencePort iRestaurantPersistencePort;
    private final ListRestaurantsDomainValidator listRestaurantsDomainValidator;

    @Override
    public Mono<PageResult<RestaurantListItem>> list(int page, int size) {
        return Mono.defer(() -> {
            listRestaurantsDomainValidator.validate(page, size);

            return Mono.zip(
                            iRestaurantPersistencePort.findActiveRestaurantsOrdered(page, size)
                                    .map(RestaurantBuilder::buildRestaurantListItem)
                                    .collectList(),
                            iRestaurantPersistencePort.countActiveRestaurants()
                    )
                    .map(tuple -> {
                        var listRestaurants = tuple.getT1();
                        long totalElements = tuple.getT2();
                        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                        return PageResult.<RestaurantListItem>builder()
                                .content(listRestaurants)
                                .page(page)
                                .size(size)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build();
                    });
        });
    }
}
