package com.pragma.order_service.infrastructure.output.postgres.mapper;

import com.pragma.order_service.domain.model.Order;
import com.pragma.order_service.infrastructure.output.postgres.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderEntityMapper {
    @Mapping(target = "items", ignore = true)
    Order toDomain(OrderEntity entity);

    OrderEntity toEntity(Order order);
}
