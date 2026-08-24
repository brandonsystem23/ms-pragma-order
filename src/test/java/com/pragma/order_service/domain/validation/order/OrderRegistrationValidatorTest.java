package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.spi.IRedisCachePort;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRegistrationValidatorTest {

    @Mock
    private IRedisCachePort iRedisCachePort;

    @Mock
    private IRestaurantPersistencePort iRestaurantPersistencePort;

    @Mock
    private IDishPersistencePort iDishPersistencePort;

    @Mock
    private IOrderPersistencePort iOrderPersistencePort;

    @InjectMocks
    private OrderRegistrationValidator orderRegistrationValidator;

    @Test
    void shouldValidateSuccessfully() {
        AuthSession authSession = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Dish dish1 = Dish.builder()
                .id(10L)
                .restaurantId(1L)
                .name("Pizza")
                .price(BigDecimal.valueOf(20000))
                .status(true)
                .build();

        Dish dish2 = Dish.builder()
                .id(11L)
                .restaurantId(1L)
                .name("Hamburguesa")
                .price(BigDecimal.valueOf(15000))
                .status(true)
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iOrderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(iDishPersistencePort.findByIdAndStatusTrue(anyLong()))
                .thenReturn(Mono.just(dish1))
                .thenReturn(Mono.just(dish2));

        StepVerifier.create(orderRegistrationValidator.validate(
                        1L,
                        List.of(
                                new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                                new CreateOrderItemCommand(11L, BigDecimal.ONE)
                        ),
                        "token-test"
                ))
                .expectNext(20L)
                .verifyComplete();
    }

    @Test
    void shouldFailWhenTokenIsInvalid() {
        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(orderRegistrationValidator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "bad-token"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Token inválido o expirado", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRoleIsNotClient() {
        AuthSession authSession = AuthSession.builder()
                .userId(20L)
                .role("PROPIETARIO")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));

        StepVerifier.create(orderRegistrationValidator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("No tienes permisos para crear pedidos", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenCustomerHasActiveOrder() {

        AuthSession authSession = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString()))
                .thenReturn(Mono.just(authSession));

        when(iRestaurantPersistencePort.existById(anyLong()))
                .thenReturn(Mono.just(true));

        when(iOrderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(
                anyLong(),
                anyLong()
        )).thenReturn(Mono.just(true));

        StepVerifier.create(orderRegistrationValidator.validate(1L, List.of(
                                        new CreateOrderItemCommand(
                                                10L,
                                                BigDecimal.valueOf(2)
                                        )), "token-test")
                )
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El cliente ya tiene un pedido en proceso para este restaurante",
                            error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenRestaurantDoesNotExist() {

        AuthSession authSession = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString()))
                .thenReturn(Mono.just(authSession));

        when(iRestaurantPersistencePort.existById(anyLong()))
                .thenReturn(Mono.just(false));

        StepVerifier.create(
                        orderRegistrationValidator.validate(
                                1L,
                                List.of(
                                        new CreateOrderItemCommand(
                                                10L,
                                                BigDecimal.valueOf(2)
                                        )
                                ),
                                "token-test"
                        )
                )
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(
                            DomainException.class,
                            error
                    );
                    Assertions.assertEquals(
                            "El restaurante no existe",
                            error.getMessage()
                    );
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishDoesNotExist() {
        AuthSession authSession = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iOrderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(iDishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(orderRegistrationValidator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("El plato no existe", error.getMessage());
                })
                .verify();
    }

    @Test
    void shouldFailWhenDishBelongsToAnotherRestaurant() {
        AuthSession authSession = AuthSession.builder()
                .userId(20L)
                .role("CLIENTE")
                .build();

        Dish dish = Dish.builder()
                .id(10L)
                .restaurantId(99L)
                .name("Pizza")
                .price(BigDecimal.valueOf(20000))
                .status(true)
                .build();

        when(iRedisCachePort.findByToken(anyString())).thenReturn(Mono.just(authSession));
        when(iOrderPersistencePort.existsByCustomerIdAndRestaurantIdAndStatusIn(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(iRestaurantPersistencePort.existById(anyLong())).thenReturn(Mono.just(true));
        when(iDishPersistencePort.findByIdAndStatusTrue(anyLong())).thenReturn(Mono.just(dish));

        StepVerifier.create(orderRegistrationValidator.validate(
                        1L,
                        List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2))),
                        "token-test"
                ))
                .expectErrorSatisfies(error -> {
                    Assertions.assertInstanceOf(DomainException.class, error);
                    Assertions.assertEquals("Todos los platos del pedido deben pertenecer al restaurante indicado", error.getMessage());
                })
                .verify();
    }
}
