package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.port.in.CreateDishUseCase;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateDishService implements CreateDishUseCase {

    private final DishPersistencePort dishPersistencePort;
    private final DishRegistrationValidator dishRegistrationValidator;
    private final DishDomainValidator dishDomainValidator;

    @Override
    public Mono<Dish> create(CreateDishCommand command, String token) {
        return Mono.defer(() -> {
            dishDomainValidator.validateForCreate(command);

            return dishRegistrationValidator.validate(
                            command.name(),
                            command.restaurantId(),
                            token
                    )
                    .then(Mono.defer(() -> {
                        Dish dish = Dish.builder()
                                .name(command.name())
                                .price(command.price())
                                .description(command.description())
                                .urlImage(command.urlImage())
                                .category(command.category())
                                .status(true)
                                .restaurantId(command.restaurantId())
                                .build();

                        return dishPersistencePort.save(dish);
                    }));
        });
    }
}
