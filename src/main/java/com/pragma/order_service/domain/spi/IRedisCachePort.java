package com.pragma.order_service.domain.spi;

import reactor.core.publisher.Mono;

public interface IRedisCachePort {

    Mono<Boolean> existsByEmployeeDocumentAndPin(String numberDocument, String pin);
}
