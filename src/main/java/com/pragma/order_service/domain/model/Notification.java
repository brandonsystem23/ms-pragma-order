package com.pragma.order_service.domain.model;

import lombok.Builder;

@Builder
public record Notification (

        String phoneNumber,
        String message
) {
}
