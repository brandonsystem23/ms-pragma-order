package com.pragma.order_service.infrastructure.configuration;

import com.pragma.order_service.domain.api.ICreateDishServicePort;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.api.ICreateRestaurantServicePort;
import com.pragma.order_service.domain.api.IListDishesServicePort;
import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import com.pragma.order_service.domain.api.IUpdateDishServicePort;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.usecase.CreateDishUseCase;
import com.pragma.order_service.domain.usecase.ListDishesUseCase;
import com.pragma.order_service.domain.usecase.UpdateDishUseCase;
import com.pragma.order_service.domain.validation.dish.DishDomainValidator;
import com.pragma.order_service.domain.validation.dish.DishRegistrationValidator;
import com.pragma.order_service.domain.validation.dish.DishRetrieveValidator;
import com.pragma.order_service.domain.validation.dish.ListDishesDomainValidator;
import com.pragma.order_service.domain.validation.dish.UpdateDishDomainValidator;
import com.pragma.order_service.domain.validation.dish.UpdateDishRegistrationValidator;
import com.pragma.order_service.domain.usecase.CreateOrderUseCase;
import com.pragma.order_service.domain.usecase.ListOrdersUseCase;
import com.pragma.order_service.domain.usecase.UpdateOrderUseCase;
import com.pragma.order_service.domain.validation.order.ListOrdersDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import com.pragma.order_service.domain.validation.order.OrderRetrieveValidator;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
import com.pragma.order_service.domain.usecase.CreateRestaurantUseCase;
import com.pragma.order_service.domain.usecase.ListRestaurantsUseCase;
import com.pragma.order_service.domain.validation.restaurant.ListRestaurantsDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantRegistrationValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantRetrieveValidator;
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
            IRestaurantPersistencePort iRestaurantPersistencePort,
            IRedisCachePort iRedisCachePort,
            IUserWebClientPort iUserWebClientPort
    ) {
        return new RestaurantRegistrationValidator(
                iRestaurantPersistencePort,
                iRedisCachePort,
                iUserWebClientPort
        );
    }

    @Bean
    public ICreateRestaurantServicePort createRestaurantUseCase(
            IRestaurantPersistencePort iRestaurantPersistencePort,
            RestaurantRegistrationValidator restaurantRegistrationValidator,
            RestaurantDomainValidator restaurantDomainValidator
    ) {
        return new CreateRestaurantUseCase(
                iRestaurantPersistencePort,
                restaurantRegistrationValidator,
                restaurantDomainValidator
        );
    }

    @Bean
    public RestaurantRetrieveValidator restaurantRetrieveValidator(
            IRedisCachePort iRedisCachePort
    ) {
        return new RestaurantRetrieveValidator(iRedisCachePort);
    }

    @Bean
    public IListRestaurantsServicePort listRestaurantsUseCase(
            IRestaurantPersistencePort iRestaurantPersistencePort,
            RestaurantRetrieveValidator restaurantClientAccessValidator,
            ListRestaurantsDomainValidator listRestaurantsDomainValidator
    ) {
        return new ListRestaurantsUseCase(
                iRestaurantPersistencePort,
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
            IRestaurantPersistencePort iRestaurantPersistencePort,
            IDishPersistencePort iDishPersistencePort,
            IRedisCachePort iRedisCachePort
    ) {
        return new DishRegistrationValidator(
                iRestaurantPersistencePort,
                iDishPersistencePort,
                iRedisCachePort
        );
    }

    @Bean
    public ICreateDishServicePort createDishUseCase(
            IDishPersistencePort iDishPersistencePort,
            DishRegistrationValidator dishRegistrationValidator,
            DishDomainValidator dishDomainValidator
    ) {
        return new CreateDishUseCase(
                iDishPersistencePort,
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
            IDishPersistencePort iDishPersistencePort,
            IRestaurantPersistencePort iRestaurantPersistencePort,
            IRedisCachePort iRedisCachePort
    ) {
        return new UpdateDishRegistrationValidator(
                iDishPersistencePort,
                iRestaurantPersistencePort,
                iRedisCachePort
        );
    }

    @Bean
    public IUpdateDishServicePort updateDishUseCase(
            IDishPersistencePort iDishPersistencePort,
            UpdateDishRegistrationValidator dishRegistrationValidator,
            UpdateDishDomainValidator updateDishDomainValidator
    ) {
        return new UpdateDishUseCase(
                iDishPersistencePort,
                dishRegistrationValidator,
                updateDishDomainValidator
        );
    }

    @Bean
    public DishRetrieveValidator dishRetrieveValidator(
            IRestaurantPersistencePort iRestaurantPersistencePort,
            IRedisCachePort iRedisCachePort
    ) {
        return new DishRetrieveValidator(iRestaurantPersistencePort, iRedisCachePort);
    }

    @Bean
    public ListDishesDomainValidator listDishesDomainValidator() {
        return new ListDishesDomainValidator();
    }

    @Bean
    public IListDishesServicePort listDishesUseCase(
            IDishPersistencePort iDishPersistencePort,
            DishRetrieveValidator dishRetrieveValidator,
            ListDishesDomainValidator listDishesDomainValidator
    ) {
        return new ListDishesUseCase(
                iDishPersistencePort,
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
            IRedisCachePort iRedisCachePort,
            IRestaurantPersistencePort iRestaurantPersistencePort,
            IDishPersistencePort iDishPersistencePort,
            IOrderPersistencePort iOrderPersistencePort
    ) {
        return new OrderRegistrationValidator(
                iRedisCachePort,
                iRestaurantPersistencePort,
                iDishPersistencePort,
                iOrderPersistencePort
        );
    }

    @Bean
    public ICreateOrderServicePort createOrderUseCase(
            IOrderPersistencePort iOrderPersistencePort,
            OrderRegistrationValidator orderRegistrationValidator,
            OrderDomainValidator orderDomainValidator
    ) {
        return new CreateOrderUseCase(
                iOrderPersistencePort,
                orderRegistrationValidator,
                orderDomainValidator
        );
    }

    @Bean
    public ListOrdersDomainValidator listOrdersDomainValidator() {
        return new ListOrdersDomainValidator();
    }

    @Bean
    public OrderRetrieveValidator orderRetrieveValidator(
            IRedisCachePort iRedisCachePort,
            IRestaurantPersistencePort iRestaurantPersistencePort
    ) {
        return new OrderRetrieveValidator(iRedisCachePort, iRestaurantPersistencePort);
    }

    @Bean
    public IListOrdersServicePort listOrdersUseCase(
            IOrderPersistencePort iOrderPersistencePort,
            OrderRetrieveValidator orderRetrieveValidator,
            ListOrdersDomainValidator listOrdersDomainValidator
    ) {
        return new ListOrdersUseCase(
                iOrderPersistencePort,
                orderRetrieveValidator,
                listOrdersDomainValidator
        );
    }

    @Bean
    public UpdateOrderDomainValidator updateOrderStatusDomainValidator() {
        return new UpdateOrderDomainValidator();
    }

    @Bean
    public IUpdateOrderServicePort updateOrderStatusUseCase(
            IOrderPersistencePort iOrderPersistencePort,
            IRedisCachePort iRedisCachePort,
            IRestaurantPersistencePort iRestaurantPersistencePort,
            IUserWebClientPort iUserWebClientPort,
            INotificationWebClientPort iNotificationWebClientPort,
            UpdateOrderDomainValidator updateOrderStatusDomainValidator
    ) {
        return new UpdateOrderUseCase(
                iOrderPersistencePort,
                iRedisCachePort,
                iRestaurantPersistencePort,
                iUserWebClientPort,
                iNotificationWebClientPort,
                updateOrderStatusDomainValidator
        );
    }
}
