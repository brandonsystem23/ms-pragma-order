package com.pragma.order_service.application.mapper;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.OrderItemResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.model.query.OrderDetail;
import com.pragma.order_service.domain.model.query.OrderQueryModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderDtoMapper {

    CreateOrderCommand toCommand(CreateOrderRequest request);

    UpdateOrderCommand toUpdateStatusCommand(UpdateOrderRequest request);

    @Mapping(target = "id", source = "orderDetail.orderId")
    @Mapping(target = "customerId", source = "orderDetail.customerId")
    @Mapping(target = "nameCustomer", source = "orderDetail.customerName")
    @Mapping(target = "restaurantId", source = "orderDetail.restaurantId")
    @Mapping(target = "nameRestaurant", source = "orderDetail.restaurantName")
    @Mapping(target = "status", source = "orderDetail.status")
    @Mapping(target = "employeeAssignedId", source = "orderDetail.employeeAssignedId")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "createdAt", source = "orderDetail.createdAt")
    @Mapping(target = "updatedAt", source = "orderDetail.updatedAt")
    OrderResponse toResponse(OrderDetail orderDetail, List<OrderItemResponse> items);

    @Mapping(target = "dishId", source = "dishId")
    @Mapping(target = "name", source = "dishName")
    @Mapping(target = "quantity", source = "quantity")
    OrderItemResponse toItemResponse(OrderDetail orderDetail);

    OrderResponse toResponse(OrderQueryModel orderQueryModel);
}
