package com.pragma.order_service.domain.port.out;

import reactor.core.publisher.Mono;

public interface OrderPinValidationPort {

    Mono<Boolean> existsByEmployeeDocumentAndPin(String numberDocument, String pin);
}
