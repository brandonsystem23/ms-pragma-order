package com.pragma.order_service.infrastructure.out.webclient.dto;

import lombok.Builder;

@Builder
public record NotificationResponse(

        String phoneNumber,
        String message
) {
}
