package com.pragma.order_service.domain.port.out;

import com.pragma.order_service.domain.model.UserSummary;
import reactor.core.publisher.Mono;

public interface UserQueryPort {

    Mono<UserSummary> findById(Long userId);
}
