package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.api.ICreateRestaurantServicePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.validation.restaurant.RestaurantDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantRegistrationValidator;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateRestaurantUseCase implements ICreateRestaurantServicePort {

    private final IRestaurantPersistencePort iRestaurantPersistencePort;
    private final RestaurantRegistrationValidator restaurantRegistrationValidator;
    private final RestaurantDomainValidator restaurantDomainValidator;

    @Override
    public Mono<Restaurant> create(CreateRestaurantCommand createRestaurantCommand, String token) {
        return Mono.defer(() -> {

            restaurantDomainValidator.validateForCreate(createRestaurantCommand);

            return restaurantRegistrationValidator.validate(
                            createRestaurantCommand.nit(),
                            createRestaurantCommand.ownerId(),
                            token
                    )
                    .then(Mono.defer(() ->
                         iRestaurantPersistencePort.save(
                                 buildToRestaurant(createRestaurantCommand)
                         )
                    ));
        });
    }

    private Restaurant buildToRestaurant(CreateRestaurantCommand createRestaurantCommand) {
        return Restaurant.builder()
                .name(createRestaurantCommand.name())
                .nit(createRestaurantCommand.nit())
                .address(createRestaurantCommand.address())
                .phone(createRestaurantCommand.phone())
                .urlLogo(createRestaurantCommand.urlLogo())
                .ownerId(createRestaurantCommand.ownerId())
                .status(true)
                .build();
    }
}
