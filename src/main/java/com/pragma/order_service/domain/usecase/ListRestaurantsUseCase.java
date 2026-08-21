package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.validation.restaurant.ListRestaurantsDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantRetrieveValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListRestaurantsUseCase implements IListRestaurantsServicePort {

    private final IRestaurantPersistencePort iRestaurantPersistencePort;
    private final RestaurantRetrieveValidator restaurantRetrieveValidator;
    private final ListRestaurantsDomainValidator listRestaurantsDomainValidator;

    @Override
    public Mono<PageResult<RestaurantListItem>> list(String token, int page, int size) {

        return Mono.defer(() -> {

            listRestaurantsDomainValidator.validate(page, size);

            return restaurantRetrieveValidator.validate(token)
                    .then(Mono.defer(() ->
                            Mono.zip(
                                    iRestaurantPersistencePort.findActiveRestaurantsOrdered(page, size)
                                            .map(this::buildToRestaurantListItem)
                                            .collectList(),
                                    iRestaurantPersistencePort.countActiveRestaurants()
                            )
                    ))
                    .map(tuple -> {
                        var content = tuple.getT1();
                        long totalElements = tuple.getT2();
                        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                        return PageResult.<RestaurantListItem>builder()
                                .content(content)
                                .page(page)
                                .size(size)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build();
                    });
        });
    }

    private RestaurantListItem buildToRestaurantListItem(Restaurant restaurant) {
        return RestaurantListItem.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .urlLogo(restaurant.getUrlLogo())
                .build();
    }
}
