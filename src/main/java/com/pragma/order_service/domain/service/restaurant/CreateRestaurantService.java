package com.pragma.order_service.domain.service.restaurant;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.port.in.CreateRestaurantUseCase;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateRestaurantService implements CreateRestaurantUseCase {

    private final RestaurantPersistencePort restaurantPersistencePort;
    private final RestaurantRegistrationValidator restaurantRegistrationValidator;
    private final RestaurantDomainValidator restaurantDomainValidator;

    @Override
    public Mono<Restaurant> create(CreateRestaurantCommand command, String token) {
        return Mono.defer(() -> {
            restaurantDomainValidator.validateForCreate(command);

            return restaurantRegistrationValidator.validate(
                            command.nit(),
                            command.ownerId(),
                            token
                    )
                    .then(Mono.defer(() -> {
                        Restaurant restaurant = Restaurant.builder()
                                .name(command.name())
                                .nit(command.nit())
                                .address(command.address())
                                .phone(command.phone())
                                .urlLogo(command.urlLogo())
                                .ownerId(command.ownerId())
                                .build();

                        return restaurantPersistencePort.save(restaurant);
                    }));
        });
    }
}
