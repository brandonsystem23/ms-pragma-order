package com.pragma.order_service.domain.spi;

import com.pragma.order_service.domain.model.UserSummary;
import reactor.core.publisher.Mono;

public interface IUserWebClientPort {

    Mono<UserSummary> findById(Long userId, String token);
}
