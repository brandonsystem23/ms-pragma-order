package com.pragma.order_service.infrastructure.out.postgres.mapper;

import com.pragma.order_service.domain.model.OrderItem;
import com.pragma.order_service.infrastructure.out.postgres.entity.OrderItemEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderItemEntityMapper {

    OrderItem toDomain(OrderItemEntity entity);

    OrderItemEntity toEntity(OrderItem orderItem);
}
