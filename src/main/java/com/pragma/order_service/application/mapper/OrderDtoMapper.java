package com.pragma.order_service.application.mapper;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderDtoMapper {

    CreateOrderCommand toCommand(CreateOrderRequest request);

    UpdateOrderCommand toUpdateStatusCommand(UpdateOrderRequest request);

    OrderResponse toResponse(OrderQueryModel orderQueryModel);
}
