package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.spi.IRedisCachePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class OrderPinValidator {

    private final IRedisCachePort redisCachePort;

    public Mono<Void> validateDeliveryPin(String numberDocument, String pin) {
        return redisCachePort.existsByEmployeeDocumentAndPin(numberDocument, pin)
                .flatMap(existsPin -> Boolean.TRUE.equals(existsPin)
                        ? Mono.empty()
                        : Mono.error(new DomainException(
                        DomainErrorCode.INVALID_PIN,
                        DomainErrorMessages.ORDER_DELIVER_INVALID_PIN
                )));
    }
}
