package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.port.in.ListDishesUseCase;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.service.dish.validation.DishRetrieveValidator;
import com.pragma.order_service.domain.service.dish.validation.ListDishesDomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListDishesService implements ListDishesUseCase {

    private final DishPersistencePort dishPersistencePort;
    private final DishRetrieveValidator dishRetrieveValidator;
    private final ListDishesDomainValidator listDishesDomainValidator;

    @Override
    public Mono<PagedResponse<DishResponse>> listByRestaurant(Long restaurantId, String category, int page, int size,
                                                              String token) {
        return Mono.defer(() -> {
            listDishesDomainValidator.validate(restaurantId, category, page, size);
            return dishRetrieveValidator.validate(token, restaurantId)
                    .then(Mono.defer(() ->
                            Mono.zip(
                                    getDishes(restaurantId, category, page, size).collectList(),
                                    countDishes(restaurantId, category)
                            )
                    ))
                    .map(tuple -> {
                        var content = tuple.getT1();
                        long totalElements = tuple.getT2();
                        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                        return PagedResponse.<DishResponse>builder()
                                .content(content)
                                .page(page)
                                .size(size)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build();
                    });
        });
    }


    private Flux<DishResponse> getDishes(Long restaurantId, String category, int page, int size) {

        Flux<Dish> dishes;

        if (category == null || category.trim().isEmpty()) {
            dishes = dishPersistencePort.findActiveByRestaurantId(restaurantId, page, size);
        } else {
            dishes = dishPersistencePort.findActiveByRestaurantIdAndCategory(restaurantId, category, page, size);
        }

        return dishes.map(dish -> DishResponse.builder()
                .id(dish.getId())
                .name(dish.getName())
                .price(dish.getPrice())
                .description(dish.getDescription())
                .urlImage(dish.getUrlImage())
                .category(dish.getCategory())
                .status(dish.getStatus())
                .restaurantId(dish.getRestaurantId())
                .createdAt(dish.getCreatedAt())
                .updatedAt(dish.getUpdatedAt())
                .build());
    }

    private Mono<Long> countDishes(Long restaurantId, String category) {
        if (category == null || category.trim().isEmpty()) {
            return dishPersistencePort.countActiveByRestaurantId(restaurantId);
        }

        return dishPersistencePort.countActiveByRestaurantIdAndCategory(restaurantId, category);
    }
}
