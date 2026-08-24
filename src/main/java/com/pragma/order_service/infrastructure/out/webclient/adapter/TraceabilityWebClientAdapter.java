package com.pragma.order_service.infrastructure.out.webclient.adapter;

import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import com.pragma.order_service.domain.spi.ITraceabilityWebClientPort;
import com.pragma.order_service.infrastructure.exception.ExternalServiceException;
import com.pragma.order_service.infrastructure.out.webclient.dto.TraceabilityResponse;
import com.pragma.order_service.infrastructure.out.webclient.mapper.TraceabilityMapper;
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
public class TraceabilityWebClientAdapter implements ITraceabilityWebClientPort {

    private final WebClient traceabilityWebClient;
    private final String traceabilityPath;
    private final TraceabilityMapper traceabilityMapper;

    public TraceabilityWebClientAdapter(
            @Qualifier("traceabilityWebClient") WebClient traceabilityWebClient,
            @Value("${clients.traceability.path}") String traceabilityPath,
            TraceabilityMapper traceabilityMapper) {

        this.traceabilityWebClient = traceabilityWebClient;
        this.traceabilityPath = traceabilityPath;
        this.traceabilityMapper = traceabilityMapper;
    }

    @Override
    public Mono<TraceabilityRecord> create(Traceability traceability, String token) {

        Map<String, String> mapHeaders = new HashMap<>();
        mapHeaders.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return bodyClientPath(traceabilityWebClient, traceabilityPath, mapHeaders, traceability)
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("Error al registrar trazabilidad")
                                .flatMap(message -> Mono.error(new ExternalServiceException(
                                                HttpStatus.valueOf(clientResponse.statusCode().value()), message))
                                )
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .defaultIfEmpty("Error interno del servicio de trazabilidad")
                                .flatMap(message -> Mono.error(new ExternalServiceException(
                                                HttpStatus.valueOf(clientResponse.statusCode().value()), message))
                                )
                )
                .bodyToMono(TraceabilityResponse.class)
                .map(traceabilityMapper::toDomain);
    }

    private WebClient.ResponseSpec bodyClientPath(
            WebClient webClient,
            String path,
            Map<String, String> mapHeaders,
            Traceability traceability) {

        log.info("Enviando trazabilidad para orderId={} con estado {}", traceability.orderId(), traceability.status());

        return webClient.post()
                .uri(path)
                .headers(httpHeaders -> mapHeaders.forEach(httpHeaders::set))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(traceabilityMapper.toRequest(traceability))
                .retrieve();
    }
}