package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.auth.AuthSession;
import reactor.core.publisher.Mono;

public interface AuthSessionPort {

    Mono<AuthSession> findByToken(String token);
}
