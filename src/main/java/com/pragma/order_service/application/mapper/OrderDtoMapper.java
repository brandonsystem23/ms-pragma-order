package com.pragma.order_service.application.mapper;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.infrastructure.output.postgres.model.OrderSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDtoMapper {

    CreateOrderCommand toCommand(CreateOrderRequest request);

    @Mapping(target = "id", source = "orderSummary.orderId" )
    @Mapping(target = "customerId", source = "orderSummary.customerId" )
    @Mapping(target = "nameCustomer", source = "orderSummary.customerName" )
    @Mapping(target = "restaurantId", source = "orderSummary.restaurantId" )
    @Mapping(target = "nameRestaurant", source = "orderSummary.restaurantName" )
    @Mapping(target = "status", source = "orderSummary.status" )
    @Mapping(target = "items", source = "items" )
    @Mapping(target = "createdAt", source = "orderSummary.createdAt" )
    @Mapping(target = "updatedAt", source = "orderSummary.updatedAt" )
    OrderResponse toResponse(OrderSummary orderSummary, List<OrderItemResponse> items);

    @Mapping(target = "dishId", source = "dishId")
    @Mapping(target = "name", source = "dishName")
    @Mapping(target = "quantity", source = "quantity")
    OrderItemResponse toItemResponse(OrderSummary orderSummary);

}
