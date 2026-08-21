package com.pragma.order_service.infrastructure.out.postgres.adapter;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.spi.IOrderPersistencePort;
import com.pragma.order_service.infrastructure.out.postgres.entity.OrderEntity;
import com.pragma.order_service.infrastructure.out.postgres.entity.OrderItemEntity;
import com.pragma.order_service.infrastructure.out.postgres.mapper.OrderEntityMapper;
import com.pragma.order_service.infrastructure.out.postgres.mapper.OrderItemEntityMapper;
import com.pragma.order_service.infrastructure.out.postgres.repository.OrderItemRepository;
import com.pragma.order_service.infrastructure.out.postgres.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements IOrderPersistencePort {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEntityMapper orderEntityMapper;
    private final OrderItemEntityMapper orderItemEntityMapper;

    @Override
    public Mono<Boolean> existsByCustomerIdAndRestaurantIdAndStatusIn(Long customerId, Long restaurantId) {
        return orderRepository.existsActiveOrderByCustomerId(customerId, restaurantId);
    }

    @Override
    public Mono<Order> save(Order order) {
        OrderEntity orderEntity = orderEntityMapper.toEntity(order);

        return orderRepository.save(orderEntity)
                .flatMap(savedOrderEntity -> {
                    if (order.getId() != null) {
                        Order savedOrder = orderEntityMapper.toDomain(savedOrderEntity);
                        savedOrder.setItems(order.getItems());
                        return Mono.just(savedOrder);
                    }

                    return saveItems(savedOrderEntity.getId(), order.getItems())
                            .map(savedItems -> buildOrder(savedOrderEntity, savedItems));
                });
    }

    @Override
    public Mono<Order> findById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(orderEntityMapper::toDomain);
    }

    @Override
    public Flux<OrderDetail> findOrderDetailById(Long orderId) {
        return orderRepository.findOrderDetailById(orderId)
                .map(orderEntityMapper::toOrderDetail);
    }

    @Override
    public Mono<Long> countOrdersByRestaurantIdAndStatus(Long restaurantId, String status) {
        return orderRepository.countOrdersByRestaurantIdAndStatus(restaurantId, status);
    }

    @Override
    public Flux<Long> findOrderIdsByRestaurantIdAndStatus(Long restaurantId, String status, int page, int size) {
        int offset = page * size;
        return orderRepository.findOrderIdsByRestaurantIdAndStatus(restaurantId, status, size, offset);
    }

    @Override
    public Flux<OrderDetail> findOrdersDetailByIds(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Flux.empty();
        }

        return orderRepository.findOrdersDetailByIds(orderIds)
                .map(orderEntityMapper::toOrderDetail);
    }

    private Mono<List<OrderItem>> saveItems(Long orderId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return Mono.just(List.of());
        }

        List<OrderItemEntity> entities = items.stream()
                .map(item -> {
                    OrderItemEntity entity = orderItemEntityMapper.toEntity(item);
                    entity.setOrderId(orderId);
                    return entity;
                })
                .toList();

        return orderItemRepository.saveAll(entities)
                .map(orderItemEntityMapper::toDomain)
                .collectList();
    }

    private Order buildOrder(OrderEntity orderEntity, List<OrderItem> items) {
        Order order = orderEntityMapper.toDomain(orderEntity);
        order.setItems(items);
        return order;
    }
}
