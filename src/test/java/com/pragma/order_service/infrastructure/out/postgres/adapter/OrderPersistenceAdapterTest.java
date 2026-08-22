package com.pragma.order_service.infrastructure.out.postgres.adapter;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.infrastructure.out.postgres.dto.OrderSummary;
import com.pragma.order_service.infrastructure.out.postgres.entity.OrderEntity;
import com.pragma.order_service.infrastructure.out.postgres.entity.OrderItemEntity;
import com.pragma.order_service.infrastructure.out.postgres.mapper.OrderEntityMapper;
import com.pragma.order_service.infrastructure.out.postgres.mapper.OrderItemEntityMapper;
import com.pragma.order_service.infrastructure.out.postgres.repository.OrderItemRepository;
import com.pragma.order_service.infrastructure.out.postgres.repository.OrderRepository;
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

import static org.mockito.ArgumentMatchers.*;
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
                .employeeAssignedId(null)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(
                        OrderItem.builder().dishId(10L).quantity(BigDecimal.valueOf(2)).price(BigDecimal.valueOf(30000)).build(),
                        OrderItem.builder().dishId(11L).quantity(BigDecimal.ONE).price(BigDecimal.valueOf(5000)).build()
                ))
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(null)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .build();

        OrderEntity savedOrderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(null)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItemEntity itemEntity1 = OrderItemEntity.builder()
                .dishId(10L)
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(30000))
                .build();

        OrderItemEntity itemEntity2 = OrderItemEntity.builder()
                .dishId(11L)
                .quantity(BigDecimal.ONE)
                .price(BigDecimal.valueOf(5000))
                .build();

        OrderItemEntity savedItemEntity1 = OrderItemEntity.builder()
                .id(1L)
                .orderId(100L)
                .dishId(10L)
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(30000))
                .build();

        OrderItemEntity savedItemEntity2 = OrderItemEntity.builder()
                .id(2L)
                .orderId(100L)
                .dishId(11L)
                .quantity(BigDecimal.ONE)
                .price(BigDecimal.valueOf(5000))
                .build();

        OrderItem savedItem1 = OrderItem.builder()
                .id(1L)
                .orderId(100L)
                .dishId(10L)
                .quantity(BigDecimal.valueOf(2))
                .price(BigDecimal.valueOf(30000))
                .build();

        OrderItem savedItem2 = OrderItem.builder()
                .id(2L)
                .orderId(100L)
                .dishId(11L)
                .quantity(BigDecimal.ONE)
                .price(BigDecimal.valueOf(5000))
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(null)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.valueOf(65000))
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
                    Assertions.assertNull(result.getEmployeeAssignedId());
                    Assertions.assertEquals("PENDIENTE", result.getStatus());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.getTotalPrice());
                    Assertions.assertEquals(2, result.getItems().size());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getItems().get(0).getPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(5000), result.getItems().get(1).getPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateExistingOrderSuccessfullyWithoutSavingItemsAgain() {
        Order order = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(30L)
                .status("EN_PREPARACION")
                .totalPrice(BigDecimal.valueOf(65000))
                .items(List.of(
                        OrderItem.builder().dishId(10L).quantity(BigDecimal.valueOf(2)).price(BigDecimal.valueOf(30000)).build()
                ))
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(30L)
                .status("EN_PREPARACION")
                .totalPrice(BigDecimal.valueOf(65000))
                .build();

        OrderEntity savedOrderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(30L)
                .status("EN_PREPARACION")
                .totalPrice(BigDecimal.valueOf(65000))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(30L)
                .status("EN_PREPARACION")
                .totalPrice(BigDecimal.valueOf(65000))
                .createdAt(savedOrderEntity.getCreatedAt())
                .updatedAt(savedOrderEntity.getUpdatedAt())
                .build();

        when(orderEntityMapper.toEntity(any())).thenReturn(orderEntity);
        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrderEntity));
        when(orderEntityMapper.toDomain(savedOrderEntity)).thenReturn(mappedOrder);

        StepVerifier.create(orderPersistenceAdapter.save(order))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
                    Assertions.assertEquals("EN_PREPARACION", result.getStatus());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.getTotalPrice());
                    Assertions.assertEquals(1, result.getItems().size());
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
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .totalPrice(BigDecimal.valueOf(65000))
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .dishPrice(BigDecimal.valueOf(30000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderSummary row2 = OrderSummary.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .totalPrice(BigDecimal.valueOf(65000))
                .dishId(11L)
                .dishName("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .dishPrice(BigDecimal.valueOf(5000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail detail1 = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .totalPrice(BigDecimal.valueOf(65000))
                .dishId(10L)
                .dishName("Hamburguesa triple")
                .quantity(BigDecimal.valueOf(2))
                .dishPrice(BigDecimal.valueOf(30000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail detail2 = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Brandon Briones")
                .restaurantId(1L)
                .restaurantName("El buen sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .totalPrice(BigDecimal.valueOf(65000))
                .dishId(11L)
                .dishName("Lomo saltado")
                .quantity(BigDecimal.ONE)
                .dishPrice(BigDecimal.valueOf(5000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderRepository.findOrderDetailById(100L)).thenReturn(Flux.just(row1, row2));
        when(orderEntityMapper.toOrderDetail(any()))
                .thenReturn(detail1)
                .thenReturn(detail2);

        StepVerifier.create(orderPersistenceAdapter.findOrderDetailById(100L))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getOrderId());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
                    Assertions.assertEquals("Brandon Briones", result.getCustomerName());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.getTotalPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(30000), result.getDishPrice());
                })
                .assertNext(result -> {
                    Assertions.assertEquals(11L, result.getDishId());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
                    Assertions.assertEquals(BigDecimal.valueOf(5000), result.getDishPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldCountOrdersByRestaurantIdAndStatusSuccessfully() {
        when(orderRepository.countOrdersByRestaurantIdAndStatus(1L, "PENDIENTE"))
                .thenReturn(Mono.just(3L));

        StepVerifier.create(orderPersistenceAdapter.countOrdersByRestaurantIdAndStatus(1L, "PENDIENTE"))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void shouldFindOrderIdsByRestaurantIdAndStatusSuccessfully() {
        when(orderRepository.findOrderIdsByRestaurantIdAndStatus(1L, "PENDIENTE", 10, 0))
                .thenReturn(Flux.just(100L, 101L));

        StepVerifier.create(orderPersistenceAdapter.findOrderIdsByRestaurantIdAndStatus(1L, "PENDIENTE", 0, 10))
                .expectNext(100L)
                .expectNext(101L)
                .verifyComplete();
    }

    @Test
    void shouldFindOrdersDetailByIdsSuccessfully() {
        LocalDateTime now = LocalDateTime.now();

        OrderSummary row = OrderSummary.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("El Buen Sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .totalPrice(BigDecimal.valueOf(40000))
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.valueOf(2))
                .dishPrice(BigDecimal.valueOf(20000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderDetail detail = OrderDetail.builder()
                .orderId(100L)
                .customerId(20L)
                .customerName("Juan Perez")
                .restaurantId(1L)
                .restaurantName("El Buen Sabor")
                .status("EN_PREPARACION")
                .employeeAssignedId(30L)
                .totalPrice(BigDecimal.valueOf(40000))
                .dishId(10L)
                .dishName("Pizza")
                .quantity(BigDecimal.valueOf(2))
                .dishPrice(BigDecimal.valueOf(20000))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(orderRepository.findOrdersDetailByIds(List.of(100L)))
                .thenReturn(Flux.just(row));

        when(orderEntityMapper.toOrderDetail(any())).thenReturn(detail);

        StepVerifier.create(orderPersistenceAdapter.findOrdersDetailByIds(List.of(100L)))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getOrderId());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
                    Assertions.assertEquals("Juan Perez", result.getCustomerName());
                    Assertions.assertEquals(BigDecimal.valueOf(40000), result.getTotalPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(20000), result.getDishPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindOrdersDetailByIdsEmpty() {
        StepVerifier.create(orderPersistenceAdapter.findOrdersDetailByIds(List.of()))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenOrderIdsIsNull() {
        StepVerifier.create(orderPersistenceAdapter.findOrdersDetailByIds(null))
                .verifyComplete();
    }

    @Test
    void shouldSaveOrderSuccessfullyWhenItemsAreNull() {
        Order order = Order.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .items(null)
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .build();

        OrderEntity savedOrderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .items(List.of())
                .build();

        when(orderEntityMapper.toEntity(order))
                .thenReturn(orderEntity);

        when(orderRepository.save(orderEntity))
                .thenReturn(Mono.just(savedOrderEntity));

        when(orderEntityMapper.toDomain(savedOrderEntity))
                .thenReturn(mappedOrder);

        StepVerifier.create(orderPersistenceAdapter.save(order))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(20L, result.getCustomerId());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                    Assertions.assertEquals("PENDIENTE", result.getStatus());
                    Assertions.assertEquals(BigDecimal.ZERO, result.getTotalPrice());
                    Assertions.assertNotNull(result.getItems());
                    Assertions.assertTrue(result.getItems().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void shouldSaveOrderSuccessfullyWhenItemsAreEmpty() {
        Order order = Order.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .items(List.of())
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .build();

        OrderEntity savedOrderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .build();

        Order mappedOrder = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .status("PENDIENTE")
                .totalPrice(BigDecimal.ZERO)
                .build();

        when(orderEntityMapper.toEntity(order))
                .thenReturn(orderEntity);

        when(orderRepository.save(orderEntity))
                .thenReturn(Mono.just(savedOrderEntity));

        when(orderEntityMapper.toDomain(savedOrderEntity))
                .thenReturn(mappedOrder);

        StepVerifier.create(orderPersistenceAdapter.save(order))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(20L, result.getCustomerId());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                    Assertions.assertEquals("PENDIENTE", result.getStatus());
                    Assertions.assertEquals(BigDecimal.ZERO, result.getTotalPrice());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindOrderByIdSuccessfully() {
        OrderEntity orderEntity = OrderEntity.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(30L)
                .status("EN_PREPARACION")
                .totalPrice(BigDecimal.valueOf(65000))
                .build();

        Order order = Order.builder()
                .id(100L)
                .customerId(20L)
                .restaurantId(1L)
                .employeeAssignedId(30L)
                .status("EN_PREPARACION")
                .totalPrice(BigDecimal.valueOf(65000))
                .build();

        when(orderRepository.findById(100L))
                .thenReturn(Mono.just(orderEntity));

        when(orderEntityMapper.toDomain(orderEntity))
                .thenReturn(order);

        StepVerifier.create(orderPersistenceAdapter.findById(100L))
                .assertNext(result -> {
                    Assertions.assertEquals(100L, result.getId());
                    Assertions.assertEquals(20L, result.getCustomerId());
                    Assertions.assertEquals(1L, result.getRestaurantId());
                    Assertions.assertEquals(30L, result.getEmployeeAssignedId());
                    Assertions.assertEquals("EN_PREPARACION", result.getStatus());
                    Assertions.assertEquals(BigDecimal.valueOf(65000), result.getTotalPrice());
                })
                .verifyComplete();
    }
}
