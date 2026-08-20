package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.infrastructure.output.webclient.dto.NotificationResponse;
import reactor.core.publisher.Mono;

public interface NotificationWebClientPort {

    Mono<NotificationResponse> sendReadyNotification(String phone, String token);
}
