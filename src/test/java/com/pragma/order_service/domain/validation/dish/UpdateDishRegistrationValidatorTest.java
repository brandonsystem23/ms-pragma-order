package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateDishRegistrationValidatorTest {

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @InjectMocks
    private UpdateDishRegistrationValidator updateDishRegistrationValidator;

    @Test
    void shouldFindActiveDishAndValidateOwnershipSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .price(BigDecimal.valueOf(25000))
                .restaurantId(10L)
                .status(true)
                .build();

        when(dishPersistencePort.findByIdAndStatusTrue(1L)).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(updateDishRegistrationValidator.findActiveDishAndValidateOwnership(1L, 2L))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals(10L, result.getRestaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailFindActiveDishAndValidateOwnershipWhenDishDoesNotExist() {
        when(dishPersistencePort.findByIdAndStatusTrue(1L)).thenReturn(Mono.empty());

        StepVerifier.create(updateDishRegistrationValidator.findActiveDishAndValidateOwnership(1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailFindActiveDishAndValidateOwnershipWhenOwnerIsInvalid() {
        Dish dish = Dish.builder()
                .id(1L)
                .restaurantId(10L)
                .status(true)
                .build();

        when(dishPersistencePort.findByIdAndStatusTrue(1L)).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(updateDishRegistrationValidator.findActiveDishAndValidateOwnership(1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Usted no es propietario del restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFindDishAndValidateOwnershipSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .restaurantId(10L)
                .status(false)
                .build();

        when(dishPersistencePort.findById(1L)).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(updateDishRegistrationValidator.findDishAndValidateOwnership(1L, 2L))
                .assertNext(result -> {
                    Assertions.assertEquals(1L, result.getId());
                    Assertions.assertEquals(10L, result.getRestaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailFindDishAndValidateOwnershipWhenDishDoesNotExist() {
        when(dishPersistencePort.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(updateDishRegistrationValidator.findDishAndValidateOwnership(1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailFindDishAndValidateOwnershipWhenOwnerIsInvalid() {
        Dish dish = Dish.builder()
                .id(1L)
                .restaurantId(10L)
                .status(false)
                .build();

        when(dishPersistencePort.findById(1L)).thenReturn(Mono.just(dish));
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(updateDishRegistrationValidator.findDishAndValidateOwnership(1L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Usted no es propietario del restaurante", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFindActiveDishByIdOrFailSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .restaurantId(10L)
                .status(true)
                .build();

        when(dishPersistencePort.findByIdAndStatusTrue(1L)).thenReturn(Mono.just(dish));

        StepVerifier.create(updateDishRegistrationValidator.findActiveDishByIdOrFail(1L))
                .assertNext(result -> Assertions.assertEquals(1L, result.getId()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFindActiveDishByIdOrFailDoesNotFindDish() {
        when(dishPersistencePort.findByIdAndStatusTrue(1L)).thenReturn(Mono.empty());

        StepVerifier.create(updateDishRegistrationValidator.findActiveDishByIdOrFail(1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFindDishByIdOrFailSuccessfully() {
        Dish dish = Dish.builder()
                .id(1L)
                .restaurantId(10L)
                .status(false)
                .build();

        when(dishPersistencePort.findById(1L)).thenReturn(Mono.just(dish));

        StepVerifier.create(updateDishRegistrationValidator.findDishByIdOrFail(1L))
                .assertNext(result -> Assertions.assertEquals(1L, result.getId()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFindDishByIdOrFailDoesNotFindDish() {
        when(dishPersistencePort.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(updateDishRegistrationValidator.findDishByIdOrFail(1L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldValidateRestaurantOwnershipSuccessfully() {
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(true));

        StepVerifier.create(updateDishRegistrationValidator.validateRestaurantOwnership(10L, 2L))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenRestaurantOwnershipIsInvalid() {
        when(restaurantPersistencePort.existByOwner(10L, 2L)).thenReturn(Mono.just(false));

        StepVerifier.create(updateDishRegistrationValidator.validateRestaurantOwnership(10L, 2L))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Usted no es propietario del restaurante", error.getMessage());
                })
                .verify();
    }
}
