package com.pragma.order_service.domain.spi;

import com.pragma.order_service.domain.model.auth.AuthSession;
import reactor.core.publisher.Mono;

public interface IRedisCachePort {

    Mono<AuthSession> findByToken(String token);

    Mono<Boolean> existsByEmployeeDocumentAndPin(String numberDocument, String pin);
}
