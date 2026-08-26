package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.api.IListDishesServicePort;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.query.PageResult;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.validation.dish.DishRetrieveValidator;
import com.pragma.order_service.domain.validation.dish.ListDishesDomainValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ListDishesUseCase implements IListDishesServicePort {

    private final IDishPersistencePort iDishPersistencePort;
    private final DishRetrieveValidator dishRetrieveValidator;
    private final ListDishesDomainValidator listDishesDomainValidator;

    @Override
    public Mono<PageResult<Dish>> listByRestaurant(Long restaurantId, String category, int page, int size) {
        return Mono.defer(() -> {
            listDishesDomainValidator.validate(restaurantId, category, page, size);

            return dishRetrieveValidator.validateRestaurantExists(restaurantId)
                    .then(Mono.defer(() ->
                            Mono.zip(
                                    getDishes(restaurantId, category, page, size).collectList(),
                                    countDishes(restaurantId, category)
                            )
                    ))
                    .map(tuple -> {
                        var listDish = tuple.getT1();
                        long totalElements = tuple.getT2();
                        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

                        return PageResult.<Dish>builder()
                                .content(listDish)
                                .page(page)
                                .size(size)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .build();
                    });
        });
    }

    private Flux<Dish> getDishes(Long restaurantId, String category, int page, int size) {
        if (category == null || category.trim().isEmpty()) {
            return iDishPersistencePort.findActiveByRestaurantId(restaurantId, page, size);
        }

        return iDishPersistencePort.findActiveByRestaurantIdAndCategory(restaurantId, category, page, size);
    }

    private Mono<Long> countDishes(Long restaurantId, String category) {
        if (category == null || category.trim().isEmpty()) {
            return iDishPersistencePort.countActiveByRestaurantId(restaurantId);
        }

        return iDishPersistencePort.countActiveByRestaurantIdAndCategory(restaurantId, category);
    }
}
