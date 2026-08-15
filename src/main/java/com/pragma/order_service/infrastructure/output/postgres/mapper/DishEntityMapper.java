package com.pragma.order_service.infrastructure.output.postgres.mapper;

import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.infrastructure.output.postgres.entity.DishEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DishEntityMapper {

    Dish toDomain(DishEntity entity);

    DishEntity toEntity(Dish dish);

}
