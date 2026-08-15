package com.pragma.order_service.infrastructure.configuration;

import com.pragma.order_service.domain.port.in.CreateRestaurantUseCase;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.domain.port.out.UserQueryPort;
import com.pragma.order_service.domain.service.CreateRestaurantService;
import com.pragma.order_service.domain.service.RestaurantDomainValidator;
import com.pragma.order_service.domain.service.RestaurantRegistrationValidator;
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
            UserQueryPort userQueryPort
    ) {
        return new RestaurantRegistrationValidator(
                restaurantPersistencePort,
                authSessionPort,
                userQueryPort
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
}
