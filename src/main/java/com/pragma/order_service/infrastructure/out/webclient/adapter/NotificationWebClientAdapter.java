package com.pragma.order_service.infrastructure.out.webclient.adapter;

import com.pragma.order_service.domain.model.Notification;
import com.pragma.order_service.domain.spi.INotificationWebClientPort;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import com.pragma.order_service.infrastructure.out.webclient.dto.NotificationResponse;
import com.pragma.order_service.infrastructure.out.webclient.dto.SendNotificationRequest;
import com.pragma.order_service.infrastructure.out.webclient.mapper.DomainMapper;
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

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class NotificationWebClientAdapter implements INotificationWebClientPort {

    private final WebClient notificationsWebClient;
    private final String notificationsPath;
    private final DomainMapper domainMapper;

    public NotificationWebClientAdapter(
            @Qualifier("notificationsWebClient") WebClient notificationsWebClient,
            @Value("${clients.notifications.path}") String notificationsPath,
            DomainMapper domainMapper) {
        this.notificationsWebClient = notificationsWebClient;
        this.notificationsPath = notificationsPath;
        this.domainMapper = domainMapper;
    }

    @Override
    public Mono<Notification> sendReadyNotification(String phone, String token) {

        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return bodyClientPath(notificationsWebClient, notificationsPath, mapHeaders, phone)
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("Error al enviar notificación")
                                .flatMap(message -> Mono.error(new ExternalServiceException(
                                                HttpStatus.valueOf(clientResponse.statusCode().value()), message))
                                )
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("Error interno del servicio de notificaciones")
                                .flatMap(message -> Mono.error(new ExternalServiceException(
                                                HttpStatus.valueOf(clientResponse.statusCode().value()), message))
                                )
                )
                .bodyToMono(NotificationResponse.class)
                .map(domainMapper::notificationToDomain);
    }

    private static WebClient.ResponseSpec bodyClientPath(
            WebClient webClient,
            String path,
            Map<String, String> mapHeaders,
            String phone) {

        log.info("Enviando notificación al teléfono: {}", phone);

        SendNotificationRequest request = SendNotificationRequest.builder()
                .phoneNumber(phone)
                .build();

        return webClient.post()
                .uri(path)
                .headers(httpHeaders -> mapHeaders.forEach(httpHeaders::set))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve();
    }
}