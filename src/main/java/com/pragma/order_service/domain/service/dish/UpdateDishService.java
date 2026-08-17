package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.port.in.UpdateDishUseCase;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateDishService implements UpdateDishUseCase {

    private final DishPersistencePort dishPersistencePort;
    private final UpdateDishRegistrationValidator updateDishRegistrationValidator;
    private final UpdateDishDomainValidator updateDishDomainValidator;

    @Override
    public Mono<Dish> update(Long dishId, UpdateDishCommand command, String token) {
        return Mono.defer(() -> {
            updateDishDomainValidator.validateForUpdate(dishId, command);

            return updateDishRegistrationValidator.validate(dishId, token)
                    .flatMap(existingDish -> {
                        if (command.price() != null) {
                            existingDish.setPrice(command.price());
                        }

                        if (command.description() != null && !command.description().trim().isEmpty()) {
                            existingDish.setDescription(command.description());
                        }

                        return dishPersistencePort.save(existingDish);
                    });
        });
    }
}
