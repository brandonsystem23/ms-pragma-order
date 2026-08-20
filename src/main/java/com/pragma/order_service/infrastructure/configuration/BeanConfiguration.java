package com.pragma.order_service.infrastructure.configuration;

import com.pragma.order_service.domain.port.in.AssignOrderUseCase;
import com.pragma.order_service.domain.port.in.CreateDishUseCase;
import com.pragma.order_service.domain.port.in.CreateOrderUseCase;
import com.pragma.order_service.domain.port.in.CreateRestaurantUseCase;
import com.pragma.order_service.domain.port.in.ListDishesUseCase;
import com.pragma.order_service.domain.port.in.ListOrdersUseCase;
import com.pragma.order_service.domain.port.in.ListRestaurantsUseCase;
import com.pragma.order_service.domain.port.in.MarkOrderReadyUseCase;
import com.pragma.order_service.domain.port.in.UpdateDishUseCase;
import com.pragma.order_service.domain.port.out.AuthSessionPort;
import com.pragma.order_service.domain.port.out.DishPersistencePort;
import com.pragma.order_service.domain.port.out.NotificationWebClientPort;
import com.pragma.order_service.domain.port.out.OrderPersistencePort;
import com.pragma.order_service.domain.port.out.RestaurantPersistencePort;
import com.pragma.order_service.domain.port.out.UserWebClientPort;
import com.pragma.order_service.domain.service.dish.CreateDishService;
import com.pragma.order_service.domain.service.dish.ListDishesService;
import com.pragma.order_service.domain.service.dish.UpdateDishService;
import com.pragma.order_service.domain.service.dish.validation.DishDomainValidator;
import com.pragma.order_service.domain.service.dish.validation.DishRegistrationValidator;
import com.pragma.order_service.domain.service.dish.validation.DishRetrieveValidator;
import com.pragma.order_service.domain.service.dish.validation.ListDishesDomainValidator;
import com.pragma.order_service.domain.service.dish.validation.UpdateDishDomainValidator;
import com.pragma.order_service.domain.service.dish.validation.UpdateDishRegistrationValidator;
import com.pragma.order_service.domain.service.order.AssignOrderService;
import com.pragma.order_service.domain.service.order.CreateOrderService;
import com.pragma.order_service.domain.service.order.ListOrdersService;
import com.pragma.order_service.domain.service.order.MarkOrderReadyService;
import com.pragma.order_service.domain.service.order.validation.AssignOrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.AssignOrderValidator;
import com.pragma.order_service.domain.service.order.validation.ListOrdersDomainValidator;
import com.pragma.order_service.domain.service.order.validation.MarkOrderReadyValidator;
import com.pragma.order_service.domain.service.order.validation.OrderDomainValidator;
import com.pragma.order_service.domain.service.order.validation.OrderRegistrationValidator;
import com.pragma.order_service.domain.service.order.validation.OrderRetrieveValidator;
import com.pragma.order_service.domain.service.restaurant.CreateRestaurantService;
import com.pragma.order_service.domain.service.restaurant.ListRestaurantsService;
import com.pragma.order_service.domain.service.restaurant.validation.ListRestaurantsDomainValidator;
import com.pragma.order_service.domain.service.restaurant.validation.RestaurantDomainValidator;
import com.pragma.order_service.domain.service.restaurant.validation.RestaurantRegistrationValidator;
import com.pragma.order_service.domain.service.restaurant.validation.RestaurantRetrieveValidator;
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
            RestaurantRetrieveValidator restaurantClientAccessValidator,
            ListRestaurantsDomainValidator listRestaurantsDomainValidator
    ) {
        return new ListRestaurantsService(
                restaurantPersistencePort,
                restaurantClientAccessValidator,
                listRestaurantsDomainValidator
        );
    }

    @Bean
    public ListRestaurantsDomainValidator listRestaurantsDomainValidator() {
        return new ListRestaurantsDomainValidator();
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

    @Bean
    public DishRetrieveValidator dishRetrieveValidator(
            RestaurantPersistencePort restaurantPersistencePort,
            AuthSessionPort authSessionPort

    ) {
        return new DishRetrieveValidator(restaurantPersistencePort, authSessionPort);
    }

    @Bean
    public ListDishesDomainValidator listDishesDomainValidator() {
        return new ListDishesDomainValidator();
    }

    @Bean
    public ListDishesUseCase listDishesUseCase(
            DishPersistencePort dishPersistencePort,
            DishRetrieveValidator dishRetrieveValidator,
            ListDishesDomainValidator listDishesDomainValidator
    ) {
        return new ListDishesService(
                dishPersistencePort,
                dishRetrieveValidator,
                listDishesDomainValidator
        );
    }

    @Bean
    public OrderDomainValidator orderDomainValidator() {
        return new OrderDomainValidator();
    }

    @Bean
    public OrderRegistrationValidator orderRegistrationValidator(
            AuthSessionPort authSessionPort,
            RestaurantPersistencePort restaurantPersistencePort,
            DishPersistencePort dishPersistencePort,
            OrderPersistencePort orderPersistencePort
    ) {
        return new OrderRegistrationValidator(
                authSessionPort,
                restaurantPersistencePort,
                dishPersistencePort,
                orderPersistencePort
        );
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            OrderPersistencePort orderPersistencePort,
            OrderRegistrationValidator orderRegistrationValidator,
            OrderDomainValidator orderDomainValidator
    ) {
        return new CreateOrderService(
                orderPersistencePort,
                orderRegistrationValidator,
                orderDomainValidator
        );
    }

    @Bean
    public AssignOrderDomainValidator assignOrderDomainValidator() {
        return new AssignOrderDomainValidator();
    }

    @Bean
    public AssignOrderValidator assignOrderValidator(
            AuthSessionPort authSessionPort,
            RestaurantPersistencePort restaurantPersistencePort,
            OrderPersistencePort orderPersistencePort
    ) {
        return new AssignOrderValidator(
                authSessionPort,
                restaurantPersistencePort,
                orderPersistencePort
        );
    }

    @Bean
    public AssignOrderUseCase assignOrderUseCase(
            OrderPersistencePort orderPersistencePort,
            AssignOrderValidator assignOrderValidator,
            AssignOrderDomainValidator assignOrderDomainValidator
    ) {
        return new AssignOrderService(
                orderPersistencePort,
                assignOrderValidator,
                assignOrderDomainValidator
        );
    }

    @Bean
    public ListOrdersDomainValidator listOrdersDomainValidator() {
        return new ListOrdersDomainValidator();
    }

    @Bean
    public OrderRetrieveValidator orderRetrieveValidator(
            AuthSessionPort authSessionPort,
            RestaurantPersistencePort restaurantPersistencePort
    ) {
        return new OrderRetrieveValidator(authSessionPort, restaurantPersistencePort);
    }

    @Bean
    public ListOrdersUseCase listOrdersUseCase(
            OrderPersistencePort orderPersistencePort,
            OrderRetrieveValidator orderRetrieveValidator,
            ListOrdersDomainValidator listOrdersDomainValidator
    ) {
        return new ListOrdersService(
                orderPersistencePort,
                orderRetrieveValidator,
                listOrdersDomainValidator
        );
    }


    @Bean
    public MarkOrderReadyValidator markOrderReadyValidator(
            AuthSessionPort authSessionPort,
            OrderPersistencePort orderPersistencePort
    ) {
        return new MarkOrderReadyValidator(
                authSessionPort,
                orderPersistencePort
        );
    }

    @Bean
    public MarkOrderReadyUseCase markOrderReadyUseCase(
            OrderPersistencePort orderPersistencePort,
            MarkOrderReadyValidator markOrderReadyValidator,
            AssignOrderDomainValidator assignOrderDomainValidator,
            UserWebClientPort userWebClientPort,
            NotificationWebClientPort notificationWebClientPort
    ) {
        return new MarkOrderReadyService(
                orderPersistencePort,
                markOrderReadyValidator,
                assignOrderDomainValidator,
                userWebClientPort,
                notificationWebClientPort
        );
    }


}
