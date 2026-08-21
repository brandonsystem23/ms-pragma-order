package com.pragma.order_service.infrastructure.out.webclient.dto;

import lombok.Builder;

@Builder
public record SendNotificationRequest(
        String phoneNumber
) {
}
