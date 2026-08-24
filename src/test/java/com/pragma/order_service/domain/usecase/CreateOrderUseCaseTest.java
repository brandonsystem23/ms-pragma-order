package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IDishPersistencePort;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.domain.validation.order.OrderDomainValidator;
import com.pragma.order_service.domain.validation.order.OrderRegistrationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private IOrderPersistencePort iOrderPersistencePort;

    @Mock
    private IDishPersistencePort iDishPersistencePort;

    @Mock
    private ITraceabilityWebClientPort iTraceabilityWebClientPort;

    @Mock
    private OrderRegistrationValidator orderRegistrationValidator;

    @Mock
    private OrderDomainValidator orderDomainValidator;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    private CreateOrderCommand createOrderCommand;
    private CreateOrderItemCommand itemCommand;
    private Dish dish;
    private Order order;
    private OrderDetail orderDetail;

    private final String token = "token-test";

    @BeforeEach
    void setUp() {

        itemCommand = new CreateOrderItemCommand(10L, BigDecimal.valueOf(2));

        createOrderCommand = new CreateOrderCommand(1L, List.of(itemCommand));

        dish = Dish.builder()
                .id(10L)
                .name("Hamburguesa")
                .description("Hamburguesa clásica")
                .price(BigDecimal.valueOf(20000))
                .category("FASTFOOD")
                .status(true)
                .restaurantId(1L)
                .build();

        order = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .build();

        orderDetail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("Restaurante Test")
                .status("PENDIENTE")
                .employeeAssignedId(null)
                .totalPrice(BigDecimal.valueOf(40000))
                .dishId(10L)
                .dishName("Hamburguesa")
                .dishPrice(BigDecimal.valueOf(20000))
                .quantity(BigDecimal.valueOf(2))
                .ownerId(30L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() {

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.just(dish));

        when(iOrderPersistencePort.save(any(Order.class)))
                .thenReturn(Mono.just(order));

        when(iOrderPersistencePort.findOrderDetailById(anyLong()))
                .thenReturn(Flux.just(orderDetail));

        when(iTraceabilityWebClientPort.create(
                any(Traceability.class),
                anyString()
        )).thenReturn(Mono.empty());

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .assertNext(result -> {

                    assertNotNull(result);

                    assertEquals(100L, result.id());

                    assertEquals(20L, result.customerId());

                    assertEquals("Juan Perez", result.nameCustomer());

                    assertEquals(1L, result.restaurantId());

                    assertEquals("Restaurante Test", result.nameRestaurant());

                    assertEquals("PENDIENTE", result.status());
                })
                .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenRegistrationValidationFails() {

        RuntimeException exception = new RuntimeException("Error validando registro");

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error ->
                        assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenFindingDishesFails() {

        RuntimeException exception = new RuntimeException("Error obteniendo platos");

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.error(exception));

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error ->
                        assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenSavingOrderFails() {

        RuntimeException exception = new RuntimeException("Error guardando pedido");

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.just(dish));

        when(iOrderPersistencePort.save(any(Order.class)))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error ->
                        assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenFindingOrderDetailFails() {

        RuntimeException exception = new RuntimeException("Error obteniendo detalle del pedido");

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.just(dish));

        when(iOrderPersistencePort.save(any(Order.class)))
                .thenReturn(Mono.just(order));

        when(iOrderPersistencePort.findOrderDetailById(anyLong()))
                .thenReturn(Flux.error(exception));

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error ->
                        assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenSendingTraceabilityFails() {

        RuntimeException exception = new RuntimeException("Error enviando trazabilidad");

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.just(dish));

        when(iOrderPersistencePort.save(any(Order.class)))
                .thenReturn(Mono.just(order));

        when(iOrderPersistencePort.findOrderDetailById(anyLong()))
                .thenReturn(Flux.just(orderDetail));

        when(iTraceabilityWebClientPort.create(
                any(Traceability.class),
                anyString()
        )).thenReturn(Mono.error(exception));

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error ->
                        assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldPropagateErrorWhenDomainValidationFails() {

        RuntimeException exception = new RuntimeException("Los datos del pedido no son válidos");

        doThrow(exception)
                .when(orderDomainValidator)
                .validateForCreate(
                        any(CreateOrderCommand.class)
                );

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error ->
                        assertSame(exception, error)
                )
                .verify();
    }

    @Test
    void shouldCreateOrderWithMultipleDishesSuccessfully() {

        CreateOrderItemCommand secondItem =
                new CreateOrderItemCommand(
                        20L,
                        BigDecimal.valueOf(1)
                );

        CreateOrderCommand command =
                new CreateOrderCommand(
                        1L,
                        List.of(
                                itemCommand,
                                secondItem
                        )
                );

        Dish secondDish = Dish.builder()
                .id(20L)
                .name("Pizza")
                .description("Pizza familiar")
                .price(BigDecimal.valueOf(30000))
                .category("PIZZA")
                .status(true)
                .restaurantId(1L)
                .build();

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.just(dish, secondDish));

        when(iOrderPersistencePort.save(any(Order.class)))
                .thenReturn(Mono.just(order));

        when(iOrderPersistencePort.findOrderDetailById(anyLong()))
                .thenReturn(Flux.just(orderDetail));

        when(iTraceabilityWebClientPort.create(
                any(Traceability.class),
                anyString()
        )).thenReturn(Mono.empty());

        StepVerifier.create(createOrderUseCase.create(command, token))
                .assertNext(result -> {

                    assertNotNull(result);

                    assertEquals(100L, result.id());

                    assertEquals(20L, result.customerId());

                    assertEquals(1L, result.restaurantId());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenOrderDetailsAreEmpty() {

        when(orderRegistrationValidator.validate(
                anyLong(),
                any(),
                anyString()
        )).thenReturn(Mono.just(20L));

        when(iDishPersistencePort.findByIds(anyList()))
                .thenReturn(Flux.just(dish));

        when(iOrderPersistencePort.save(any(Order.class)))
                .thenReturn(Mono.just(order));

        when(iOrderPersistencePort.findOrderDetailById(anyLong()))
                .thenReturn(Flux.empty());

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, token))
                .expectErrorSatisfies(error -> {
                    assertEquals(
                            java.util.NoSuchElementException.class,
                            error.getClass()
                    );
                })
                .verify();
    }
}