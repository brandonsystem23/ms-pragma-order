package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.builder.DishBuilder;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.api.ICreateDishServicePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.validation.dish.DishDomainValidator;
import com.pragma.order_service.domain.validation.dish.DishRegistrationValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateDishUseCase implements ICreateDishServicePort {

    private final IDishPersistencePort iDishPersistencePort;
    private final DishRegistrationValidator dishRegistrationValidator;
    private final DishDomainValidator dishDomainValidator;

    @Override
    public Mono<Dish> create(CreateDishCommand createDishCommand, String token) {
        return Mono.defer(() -> {

            dishDomainValidator.validateForCreate(createDishCommand);

            return dishRegistrationValidator.validate(
                            createDishCommand.name(),
                            createDishCommand.restaurantId(),
                            token
                    )
                    .then(Mono.defer(() ->
                         iDishPersistencePort.save(
                                 DishBuilder.buildToDish(createDishCommand)
                         )
                    ));
        });
    }

}
