package com.pragma.order_service.domain.service.restaurant;

import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.domain.port.in.ListRestaurantsUseCase;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListRestaurantsService implements ListRestaurantsUseCase {

    private final RestaurantPersistencePort restaurantPersistencePort;
    private final RestaurantRetrieveValidator restaurantRetrieveValidator;

    @Override
    public Mono<PagedResponse<RestaurantListItemResponse>> list(String token, int page, int size) {
        return restaurantRetrieveValidator.validate(token)
                .then(Mono.zip(
                        restaurantPersistencePort.findActiveRestaurantsOrdered(page, size)
                                .map(restaurant -> RestaurantListItemResponse.builder()
                                        .name(restaurant.getName())
                                        .urlLogo(restaurant.getUrlLogo())
                                        .build())
                                .collectList(),
                        restaurantPersistencePort.countActiveRestaurants()
                ))
                .map(tuple -> {
                    var content = tuple.getT1();
                    long totalElements = tuple.getT2();
                    int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                    return PagedResponse.<RestaurantListItemResponse>builder()
                            .content(content)
                            .page(page)
                            .size(size)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .build();
                });
    }
}
