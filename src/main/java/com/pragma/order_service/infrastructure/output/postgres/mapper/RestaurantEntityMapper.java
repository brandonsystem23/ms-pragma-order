package com.pragma.order_service.infrastructure.output.postgres.mapper;

import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.infrastructure.output.postgres.entity.RestaurantEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantEntityMapper {

    Restaurant toDomain(RestaurantEntity entity);

    RestaurantEntity toEntity(Restaurant restaurant);

}
