package com.pragma.order_service.application.mapper;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.domain.model.Restaurant;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.model.query.RestaurantListItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestaurantDtoMapper {

    CreateRestaurantCommand toCommand(CreateRestaurantRequest request);

    RestaurantResponse toResponse(Restaurant restaurant);

    RestaurantListItemResponse toResponse(RestaurantListItem restaurantListItem);
}
