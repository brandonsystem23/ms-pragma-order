package com.pragma.order_service.infrastructure.out.webclient.mapper;

import com.pragma.order_service.domain.model.Notification;
import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.infrastructure.out.webclient.dto.NotificationResponse;
import com.pragma.order_service.infrastructure.out.webclient.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DomainMapper {

    UserSummary userToDomain(UserResponse userResponse);

    Notification notificationToDomain(NotificationResponse notificationResponse);

}
