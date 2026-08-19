package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.infrastructure.output.postgres.entity.OrderEntity;
import com.pragma.order_service.infrastructure.output.postgres.entity.OrderItemEntity;
import com.pragma.order_service.infrastructure.output.postgres.mapper.OrderEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.mapper.OrderItemEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import com.pragma.order_service.infrastructure.output.postgres.repository.OrderItemRepository;
import com.pragma.order_service.infrastructure.output.postgres.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderEntityMapper orderEntityMapper;

    @Mock
    private OrderItemEntityMapper orderItemEntityMapper;

    @InjectMocks
    private OrderPersistenceAdapter orderPersistenceAdapter;

    @Test
    void shouldReturnTrueWhenCustomerHasActiveOrder() {
        when(orderRepository.existsActiveOrderByCustomerId(anyLong(), anyLong()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(orderPersistenceAdapter.existsByCustomerIdAndRestaurantIdAndStatusIn(20L, 1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void shouldSaveOrderSuccessfullyWithItems() {
        Order order = Order.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .items(List.of(
                        OrderItem.builder().dishId(10L).quantity(BigDecimal.valueOf(2)).build(),
                        OrderItem.builder().dishId(11L).quantity(BigDecimal.ONE).build()
                ))
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .build();

        OrderEntity savedOrderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItemEntity itemEntity1 = OrderItemEntity.builder()
                .dishId(10L)
                .quantity(BigDecimal.valueOf(2))
                .build();

        OrderItemEntity itemEntity2 = OrderItemEntity.builder()
                .dishId(11L)
                .quantity(BigDecimal.ONE)
                .build();

        OrderItemEntity savedItemEntity1 = OrderItemEntity.builder()
                .id(1L)
                .orderId(100L)
                .dishId(10L)
                .quantity(BigDecimal.valueOf(2))
                .build();

        OrderItemEntity savedItemEntity2 = OrderItemEntity.builder()
                .id(2L)
                .orderId(100L)
                .dishId(11L)
                .quantity(BigDecimal.ONE)
                .build();

        OrderItem savedItem1 = OrderItem.builder()
                .id(1L)
                .orderId(100L)
                .dishId(10L)
                .quantity(BigDecimal.valueOf(2))
                .build();

        OrderItem savedItem2 = OrderItem.builder()
                .id(2L)
                .orderId(100L)
                .dishId(11L)
                .quantity(BigDecimal.ONE)
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .createdAt(savedOrderEntity.getCreatedAt())
                .updatedAt(savedOrderEntity.getUpdatedAt())
                .build();

        when(orderEntityMapper.toEntity(any())).thenReturn(orderEntity);
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrderEntity));

        when(orderItemEntityMapper.toEntity(any()))
                .thenReturn(itemEntity1)
                .thenReturn(itemEntity2);

        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(savedItemEntity1, savedItemEntity2));

        when(orderItemEntityMapper.toDomain(savedItemEntity1)).thenReturn(savedItem1);
        when(orderItemEntityMapper.toDomain(savedItemEntity2)).thenReturn(savedItem2);

        when(orderEntityMapper.toDomain(savedOrderEntity)).thenReturn(mappedOrder);

        StepVerifier.create(orderPersistenceAdapter.save(order))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(20L, result.getCustomerId());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                    Assertions.assertEquals("PENDIENTE", result.getStatus());
                    Assertions.assertEquals(2, result.getItems().size());
                    Assertions.assertEquals(10L, result.getItems().get(0).getDishId());
                    Assertions.assertEquals(BigDecimal.valueOf(2), result.getItems().get(0).getQuantity());
                    Assertions.assertEquals(11L, result.getItems().get(1).getDishId());
                    Assertions.assertEquals(BigDecimal.ONE, result.getItems().get(1).getQuantity());
                })
                .verifyComplete();
    }

    @Test
    void shouldSaveOrderSuccessfullyWithoutItems() {
        Order order = Order.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .items(List.of())
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .build();

        OrderEntity savedOrderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .createdAt(savedOrderEntity.getCreatedAt())
                .updatedAt(savedOrderEntity.getUpdatedAt())
                .build();

        when(orderEntityMapper.toEntity(any())).thenReturn(orderEntity);
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrderEntity));
        when(orderEntityMapper.toDomain(savedOrderEntity)).thenReturn(mappedOrder);

        StepVerifier.create(orderPersistenceAdapter.save(order))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertTrue(result.getItems().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindOrderDetailByIdSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        OrderSummary row1 = OrderSummary.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderSummary row2 = OrderSummary.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("PENDIENTE")
                .dishId(11L)
                .dishName("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderRepository.findOrderDetailById(100L)).thenReturn(Flux.just(row1, row2));

        StepVerifier.create(orderPersistenceAdapter.findOrderDetailById(100L))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getOrderId());
                    Assertions.assertEquals("Brandon Briones", result.getCustomerName());
                    Assertions.assertEquals("El buen sabor", result.getRestaurantName());
                    Assertions.assertEquals("Hamburguesa triple", result.getDishName());
                    Assertions.assertEquals(BigDecimal.valueOf(2), result.getQuantity());
                })
                .assertNext(result -> {
                    Assertions.assertEquals(11L, result.getDishId());
                    Assertions.assertEquals("Lomo saltado", result.getDishName());
                    Assertions.assertEquals(BigDecimal.ONE, result.getQuantity());
                })
                .verifyComplete();
    }
}
