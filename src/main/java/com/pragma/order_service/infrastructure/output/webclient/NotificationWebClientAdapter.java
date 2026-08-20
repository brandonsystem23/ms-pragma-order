package com.pragma.order_service.infrastructure.output.webclient;

import com.pragma.order_service.domain.port.out.NotificationWebClientPort;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import com.pragma.order_service.infrastructure.output.webclient.dto.NotificationResponse;
import com.pragma.order_service.infrastructure.output.webclient.dto.SendNotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class NotificationWebClientAdapter implements NotificationWebClientPort {

    private final WebClient notificationsWebClient;
    private final String notificationsPath;

    public NotificationWebClientAdapter(
            @Qualifier("notificationsWebClient") WebClient notificationsWebClient,
            @Value("${clients.notifications.path}") String notificationsPath
    ) {
        this.notificationsWebClient = notificationsWebClient;
        this.notificationsPath = notificationsPath;
    }

    @Override
    public Mono<NotificationResponse> sendReadyNotification(String phone, String token) {
        log.info("Enviando notificación de pedido listo al teléfono: {}", phone);

        return notificationsWebClient.post()
                .uri(notificationsPath)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new SendNotificationRequest(phone))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .defaultIfEmpty("Error al enviar notificación")
                        .flatMap(message -> Mono.error(new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .defaultIfEmpty("Error interno del servicio de notificaciones")
                        .flatMap(message -> Mono.error(new ExternalServiceException(
                                HttpStatus.valueOf(clientResponse.statusCode().value()),
                                message
                        )))
                )
                .bodyToMono(NotificationResponse.class);
    }
}
