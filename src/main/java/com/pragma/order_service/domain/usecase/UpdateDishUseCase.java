package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.api.IUpdateDishServicePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.validation.dish.UpdateDishDomainValidator;
import com.pragma.order_service.domain.validation.dish.UpdateDishRegistrationValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateDishUseCase implements IUpdateDishServicePort {

    private final IDishPersistencePort iDishPersistencePort;
    private final UpdateDishRegistrationValidator updateDishRegistrationValidator;
    private final UpdateDishDomainValidator updateDishDomainValidator;

    @Override
    public Mono<Dish> update(Long dishId, UpdateDishCommand updateDishCommand, String token) {
        return Mono.defer(() -> {

            updateDishDomainValidator.validateForUpdate(dishId, updateDishCommand);

            return updateDishRegistrationValidator.validate(dishId, token, null)
                    .flatMap(existingDish -> {

                        if (updateDishCommand.price() != null) {
                            existingDish.setPrice(updateDishCommand.price());
                        }

                        if (updateDishCommand.description() != null &&
                                !updateDishCommand.description().trim().isEmpty()) {
                            existingDish.setDescription(updateDishCommand.description());
                        }

                        return iDishPersistencePort.save(existingDish);
                    });
        });
    }

    @Override
    public Mono<Dish> updateStatus(Long dishId, Boolean status, String token) {
        return Mono.defer(() -> {

            updateDishDomainValidator.validateForUpdateStatus(dishId, status);

            return updateDishRegistrationValidator.validate(dishId, token, status)
                    .flatMap(existingDish -> {

                        existingDish.setStatus(status);

                        return iDishPersistencePort.save(existingDish);
                    });
        });
    }
}
