package com.pragma.order_service.domain.port.out;


import com.pragma.order_service.infrastructure.output.webclient.dto.UserResponse;
import reactor.core.publisher.Mono;

public interface UserWebClientPort {

    Mono<UserResponse> findById(Long userId, String token);
}
