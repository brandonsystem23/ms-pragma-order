package com.pragma.order_service.infrastructure.configuration;

import com.pragma.order_service.domain.api.ICreateDishServicePort;
import com.pragma.order_service.domain.api.ICreateOrderServicePort;
import com.pragma.order_service.domain.api.ICreateRestaurantServicePort;
import com.pragma.order_service.domain.api.IListDishesServicePort;
import com.pragma.order_service.domain.api.IListOrdersServicePort;
import com.pragma.order_service.domain.api.IListRestaurantsServicePort;
import com.pragma.order_service.domain.api.IUpdateDishServicePort;
import com.pragma.order_service.domain.api.IUpdateOrderServicePort;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.spi.IUserWebClientPort;
import com.pragma.order_service.domain.usecase.CreateDishUseCase;
import com.pragma.order_service.domain.usecase.CreateOrderUseCase;
import com.pragma.order_service.domain.usecase.CreateRestaurantUseCase;
import com.pragma.order_service.domain.usecase.ListDishesUseCase;
import com.pragma.order_service.domain.usecase.ListOrdersUseCase;
import com.pragma.order_service.domain.usecase.ListRestaurantsUseCase;
import com.pragma.order_service.domain.usecase.UpdateDishUseCase;
import com.pragma.order_service.domain.usecase.UpdateOrderUseCase;
import com.pragma.order_service.domain.validation.dish.DishDomainValidator;
import com.pragma.order_service.domain.validation.dish.DishRegistrationValidator;
import com.pragma.order_service.domain.validation.dish.DishRetrieveValidator;
import com.pragma.order_service.domain.validation.dish.ListDishesDomainValidator;
import com.pragma.order_service.domain.validation.dish.UpdateDishDomainValidator;
import com.pragma.order_service.domain.validation.dish.UpdateDishRegistrationValidator;
import com.pragma.order_service.domain.validation.order.ListOrdersDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderPinValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import com.pragma.order_service.domain.validation.order.OrderRetrieveValidator;
import com.pragma.order_service.domain.validation.order.OrderStatusUpdateValidator;
import com.pragma.order_service.domain.validation.order.UpdateOrderDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.ListRestaurantsDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantDomainValidator;
import com.pragma.order_service.domain.validation.restaurant.RestaurantRegistrationValidator;
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
            IRestaurantPersistencePort restaurantPersistencePort,
            IUserWebClientPort userWebClientPort
    ) {
        return new RestaurantRegistrationValidator(
                restaurantPersistencePort,
                userWebClientPort
        );
    }

    @Bean
    public ICreateRestaurantServicePort createRestaurantUseCase(
            IRestaurantPersistencePort restaurantPersistencePort,
            RestaurantRegistrationValidator restaurantRegistrationValidator,
            RestaurantDomainValidator restaurantDomainValidator
    ) {
        return new CreateRestaurantUseCase(
                restaurantPersistencePort,
                restaurantRegistrationValidator,
                restaurantDomainValidator
        );
    }

    @Bean
    public ListRestaurantsDomainValidator listRestaurantsDomainValidator() {
        return new ListRestaurantsDomainValidator();
    }

    @Bean
    public IListRestaurantsServicePort listRestaurantsUseCase(
            IRestaurantPersistencePort restaurantPersistencePort,
            ListRestaurantsDomainValidator listRestaurantsDomainValidator
    ) {
        return new ListRestaurantsUseCase(
                restaurantPersistencePort,
                listRestaurantsDomainValidator
        );
    }

    @Bean
    public DishDomainValidator dishDomainValidator() {
        return new DishDomainValidator();
    }

    @Bean
    public DishRegistrationValidator dishRegistrationValidator(
            IRestaurantPersistencePort restaurantPersistencePort,
            IDishPersistencePort dishPersistencePort
    ) {
        return new DishRegistrationValidator(
                restaurantPersistencePort,
                dishPersistencePort
        );
    }

    @Bean
    public ICreateDishServicePort createDishUseCase(
            IDishPersistencePort dishPersistencePort,
            DishRegistrationValidator dishRegistrationValidator,
            DishDomainValidator dishDomainValidator
    ) {
        return new CreateDishUseCase(
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
            IDishPersistencePort dishPersistencePort,
            IRestaurantPersistencePort restaurantPersistencePort
    ) {
        return new UpdateDishRegistrationValidator(
                dishPersistencePort,
                restaurantPersistencePort
        );
    }

    @Bean
    public IUpdateDishServicePort updateDishUseCase(
            IDishPersistencePort dishPersistencePort,
            UpdateDishRegistrationValidator updateDishRegistrationValidator,
            UpdateDishDomainValidator updateDishDomainValidator
    ) {
        return new UpdateDishUseCase(
                dishPersistencePort,
                updateDishRegistrationValidator,
                updateDishDomainValidator
        );
    }

    @Bean
    public DishRetrieveValidator dishRetrieveValidator(
            IRestaurantPersistencePort restaurantPersistencePort
    ) {
        return new DishRetrieveValidator(restaurantPersistencePort);
    }

    @Bean
    public ListDishesDomainValidator listDishesDomainValidator() {
        return new ListDishesDomainValidator();
    }

    @Bean
    public IListDishesServicePort listDishesUseCase(
            IDishPersistencePort dishPersistencePort,
            DishRetrieveValidator dishRetrieveValidator,
            ListDishesDomainValidator listDishesDomainValidator
    ) {
        return new ListDishesUseCase(
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
            IRestaurantPersistencePort restaurantPersistencePort,
            IDishPersistencePort dishPersistencePort,
            IOrderPersistencePort orderPersistencePort
    ) {
        return new OrderRegistrationValidator(
                restaurantPersistencePort,
                dishPersistencePort,
                orderPersistencePort
        );
    }

    @Bean
    public ICreateOrderServicePort createOrderUseCase(
            IOrderPersistencePort orderPersistencePort,
            IDishPersistencePort dishPersistencePort,
            ITraceabilityWebClientPort traceabilityWebClientPort,
            OrderRegistrationValidator orderRegistrationValidator,
            OrderDomainValidator orderDomainValidator
    ) {
        return new CreateOrderUseCase(
                orderPersistencePort,
                dishPersistencePort,
                traceabilityWebClientPort,
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
            IRestaurantPersistencePort restaurantPersistencePort
    ) {
        return new OrderRetrieveValidator(restaurantPersistencePort);
    }

    @Bean
    public IListOrdersServicePort listOrdersUseCase(
            IOrderPersistencePort orderPersistencePort,
            OrderRetrieveValidator orderRetrieveValidator,
            ListOrdersDomainValidator listOrdersDomainValidator
    ) {
        return new ListOrdersUseCase(
                orderPersistencePort,
                orderRetrieveValidator,
                listOrdersDomainValidator
        );
    }

    @Bean
    public UpdateOrderDomainValidator updateOrderStatusDomainValidator() {
        return new UpdateOrderDomainValidator();
    }

    @Bean
    public OrderPinValidator orderPinValidator(IRedisCachePort redisCachePort) {
        return new OrderPinValidator(redisCachePort);
    }

    @Bean
    public OrderStatusUpdateValidator orderStatusUpdateValidator(
            IRestaurantPersistencePort restaurantPersistencePort
    ) {
        return new OrderStatusUpdateValidator(restaurantPersistencePort);
    }

    @Bean
    public IUpdateOrderServicePort updateOrderStatusUseCase(
            IOrderPersistencePort orderPersistencePort,
            IUserWebClientPort userWebClientPort,
            INotificationWebClientPort notificationWebClientPort,
            ITraceabilityWebClientPort traceabilityWebClientPort,
            UpdateOrderDomainValidator updateOrderStatusDomainValidator,
            OrderStatusUpdateValidator orderStatusUpdateValidator,
            OrderPinValidator orderPinValidator
    ) {
        return new UpdateOrderUseCase(
                orderPersistencePort,
                userWebClientPort,
                notificationWebClientPort,
                traceabilityWebClientPort,
                updateOrderStatusDomainValidator,
                orderStatusUpdateValidator,
                orderPinValidator
        );
    }
}
