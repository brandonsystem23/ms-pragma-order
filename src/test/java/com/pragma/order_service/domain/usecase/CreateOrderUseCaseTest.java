package com.pragma.order_service.domain.usecase;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.TraceabilityRecord;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private ITraceabilityWebClientPort traceabilityWebClientPort;

    @Mock
    private OrderRegistrationValidator orderRegistrationValidator;

    @Mock
    private OrderDomainValidator orderDomainValidator;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    private CreateOrderCommand createOrderCommand;
    private Dish dish;
    private Order order;
    private OrderDetail orderDetail;

    @BeforeEach
    void setUp() {
        createOrderCommand = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)))
        );

        dish = Dish.builder()
                .id(10L)
                .name("Hamburguesa")
                .price(BigDecimal.valueOf(20000))
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
        doNothing().when(orderDomainValidator).validateForCreate(createOrderCommand);
        when(orderRegistrationValidator.validateOrderCreationRules(1L, createOrderCommand.items(), 20L))
                .thenReturn(Mono.empty());
        when(dishPersistencePort.findByIds(List.of(10L))).thenReturn(Flux.just(dish));
        when(orderPersistencePort.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderPersistencePort.findOrderDetailById(100L)).thenReturn(Flux.just(orderDetail));
        when(traceabilityWebClientPort.create(any(Traceability.class), any()))
                .thenReturn(Mono.just(TraceabilityRecord.builder()
                        .id("trace-1")
                        .orderId(100L)
                        .build()));

        StepVerifier.create(createOrderUseCase.create(createOrderCommand, 20L, "token-test"))
                .expectNextMatches(result ->
                        result.id().equals(100L)
                                && result.customerId().equals(20L)
                                && result.restaurantId().equals(1L)
                )
                .verifyComplete();
    }
}
