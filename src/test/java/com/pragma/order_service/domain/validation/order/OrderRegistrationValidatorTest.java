package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.IRestaurantPersistencePort;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRegistrationValidatorTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @InjectMocks
    private OrderRegistrationValidator orderRegistrationValidator;

    @Test
    void shouldValidateOrderCreationRulesSuccessfully() {
        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(1L)
                .status(true)
                .build();

        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(true));
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(20L, 1L)).thenReturn(Mono.just(false));
        when(dishPersistencePort.findByIdAndStatusTrue(10L)).thenReturn(Mono.just(dish));

        StepVerifier.create(
                        orderRegistrationValidator.validateOrderCreationRules(
                                1L,
                                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                                20L
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(
                        orderRegistrationValidator.validateOrderCreationRules(
                                1L,
                                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                                20L
                        )
                )
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateCustomerHasNoActiveOrderSuccessfully() {
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(20L, 1L))
                .thenReturn(Mono.just(false));

        StepVerifier.create(orderRegistrationValidator.validateCustomerHasNoActiveOrder(20L, 1L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCustomerHasActiveOrder() {
        when(orderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(20L, 1L))
                .thenReturn(Mono.just(true));

        StepVerifier.create(orderRegistrationValidator.validateCustomerHasNoActiveOrder(20L, 1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El cliente ya tiene un pedido en proceso para este restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateRestaurantExistsSuccessfully() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(true));

        StepVerifier.create(orderRegistrationValidator.validateRestaurantExists(1L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenValidatingRestaurantExistsAndRestaurantDoesNotExist() {
        when(restaurantPersistencePort.existById(1L)).thenReturn(Mono.just(false));

        StepVerifier.create(orderRegistrationValidator.validateRestaurantExists(1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El restaurante no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateAllDishesBelongToRestaurantSuccessfully() {
        Dish dish1 = Dish.builder()
                .id(10L)
                .restaurantId(1L)
                .status(true)
                .build();

        Dish dish2 = Dish.builder()
                .id(11L)
                .restaurantId(1L)
                .status(true)
                .build();

        when(dishPersistencePort.findByIdAndStatusTrue(10L)).thenReturn(Mono.just(dish1));
        when(dishPersistencePort.findByIdAndStatusTrue(11L)).thenReturn(Mono.just(dish2));

        StepVerifier.create(
                        orderRegistrationValidator.validateAllDishesBelongToRestaurant(
                                List.of(
                                        new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                                        new CreateOrderItemCommand(11L, BigDecimal.ONE)
                                ),
                                1L
                        )
                )
                .verifyComplete();
    }

    @Test
    void shouldFailWhenOneDishDoesNotBelongToRestaurant() {
        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(2L)
                .status(true)
                .build();

        when(dishPersistencePort.findByIdAndStatusTrue(10L)).thenReturn(Mono.just(dish));

        StepVerifier.create(
                        orderRegistrationValidator.validateAllDishesBelongToRestaurant(
                                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                                1L
                        )
                )
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Todos los platos del pedido deben pertenecer al restaurante indicado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFindActiveDishByIdOrFailSuccessfully() {
        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(1L)
                .status(true)
                .build();

        when(dishPersistencePort.findByIdAndStatusTrue(10L)).thenReturn(Mono.just(dish));

        StepVerifier.create(orderRegistrationValidator.findActiveDishByIdOrFail(10L))
                .assertNext(result -> {
                    Assertions.assertEquals(10L, result.getId());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenDishDoesNotExist() {
        when(dishPersistencePort.findByIdAndStatusTrue(10L)).thenReturn(Mono.empty());

        StepVerifier.create(orderRegistrationValidator.findActiveDishByIdOrFail(10L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateDishBelongsToRestaurantSuccessfully() {
        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(1L)
                .status(true)
                .build();

        StepVerifier.create(orderRegistrationValidator.validateDishBelongsToRestaurant(dish, 1L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenDishBelongsToDifferentRestaurant() {
        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(2L)
                .status(true)
                .build();

        StepVerifier.create(orderRegistrationValidator.validateDishBelongsToRestaurant(dish, 1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Todos los platos del pedido deben pertenecer al restaurante indicado", error.getMessage());
                })
                .verify();
    }
}
