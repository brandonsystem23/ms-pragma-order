package com.pragma.order_service.domain.spi;

import com.pragma.order_service.domain.model.Notification;
import reactor.core.publisher.Mono;

public interface INotificationWebClientPort {

    Mono<Notification> sendReadyNotification(String phone, String token);
}
