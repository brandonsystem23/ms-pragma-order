package com.pragma.order_service.application.mapper;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.domain.model.Dish;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DishDtoMapper {

    CreateDishCommand toCommand(CreateDishRequest request);

    UpdateDishCommand toUpdateCommand(UpdateDishRequest request);

    DishResponse toResponse(Dish dish);
}
