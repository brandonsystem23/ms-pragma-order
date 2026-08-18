package com.pragma.order_service.infrastructure.configuration;

import com.pragma.order_service.domain.port.in.CreateDishUseCase;
import com.pragma.order_service.domain.port.in.CreateRestaurantUseCase;
import com.pragma.order_service.domain.port.in.ListRestaurantsUseCase;
import com.pragma.order_service.domain.port.in.UpdateDishUseCase;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.domain.port.out.UserWebClientPort;
import com.pragma.order_service.domain.service.dish.CreateDishService;
import com.pragma.order_service.domain.service.dish.DishDomainValidator;
import com.pragma.order_service.domain.service.dish.DishRegistrationValidator;
import com.pragma.order_service.domain.service.dish.UpdateDishDomainValidator;
import com.pragma.order_service.domain.service.dish.UpdateDishRegistrationValidator;
import com.pragma.order_service.domain.service.dish.UpdateDishService;
import com.pragma.order_service.domain.service.restaurant.CreateRestaurantService;
import com.pragma.order_service.domain.service.restaurant.ListRestaurantsService;
import com.pragma.order_service.domain.service.restaurant.RestaurantRetrieveValidator;
import com.pragma.order_service.domain.service.restaurant.RestaurantDomainValidator;
import com.pragma.order_service.domain.service.restaurant.RestaurantRegistrationValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public RestaurantDomainValidator restaurantDomainValidator() {
        return new RestaurantDomainValidator();
    }

    @Bean
    public RestaurantRegistrationValidator restaurantRegistrationValidator(
            RestaurantPersistencePort restaurantPersistencePort,
            AuthSessionPort authSessionPort,
            UserWebClientPort userWebClientPort
    ) {
        return new RestaurantRegistrationValidator(
                restaurantPersistencePort,
                authSessionPort,
                userWebClientPort
        );
    }

    @Bean
    public CreateRestaurantUseCase createRestaurantUseCase(
            RestaurantPersistencePort restaurantPersistencePort,
            RestaurantRegistrationValidator restaurantRegistrationValidator,
            RestaurantDomainValidator restaurantDomainValidator
    ) {
        return new CreateRestaurantService(
                restaurantPersistencePort,
                restaurantRegistrationValidator,
                restaurantDomainValidator
        );
    }

    @Bean
    public RestaurantRetrieveValidator restaurantRetrieveValidator(
            AuthSessionPort authSessionPort
    ) {
        return new RestaurantRetrieveValidator(authSessionPort);
    }

    @Bean
    public ListRestaurantsUseCase listRestaurantsUseCase(
            RestaurantPersistencePort restaurantPersistencePort,
            RestaurantRetrieveValidator restaurantClientAccessValidator
    ) {
        return new ListRestaurantsService(
                restaurantPersistencePort,
                restaurantClientAccessValidator
        );
    }

    @Bean
    public DishDomainValidator dishDomainValidator() {
        return new DishDomainValidator();
    }

    @Bean
    public DishRegistrationValidator dishRegistrationValidator(
            RestaurantPersistencePort restaurantPersistencePort,
            DishPersistencePort dishPersistencePort,
            AuthSessionPort authSessionPort

    ) {
        return new DishRegistrationValidator(
                restaurantPersistencePort,
                dishPersistencePort,
                authSessionPort
        );
    }

    @Bean
    public CreateDishUseCase createDishUseCase(
            DishPersistencePort dishPersistencePort,
            DishRegistrationValidator dishRegistrationValidator,
            DishDomainValidator dishDomainValidator
    ) {
        return new CreateDishService(
                dishPersistencePort,
                dishRegistrationValidator,
                dishDomainValidator
        );
    }

    @Bean
    public UpdateDishDomainValidator updateDishDomainValidator() {
        return new UpdateDishDomainValidator();
    }

    @Bean
    public UpdateDishRegistrationValidator updateDishRegistrationValidator(
            DishPersistencePort dishPersistencePort,
            RestaurantPersistencePort restaurantPersistencePort,
            AuthSessionPort authSessionPort
    ) {
        return new UpdateDishRegistrationValidator(
                dishPersistencePort,
                restaurantPersistencePort,
                authSessionPort
        );
    }

    @Bean
    public UpdateDishUseCase updateDishUseCase(
            DishPersistencePort dishPersistencePort,
            UpdateDishRegistrationValidator updateDishRegistrationValidator,
            UpdateDishDomainValidator updateDishDomainValidator
    ) {
        return new UpdateDishService(
                dishPersistencePort,
                updateDishRegistrationValidator,
                updateDishDomainValidator
        );
    }
}
