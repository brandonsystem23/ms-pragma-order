package com.pragma.order_service.infrastructure.output.postgres.adapter;

import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.domain.port.out.UserQueryPort;
import com.pragma.order_service.infrastructure.output.postgres.mapper.UserBasicEntityMapper;
import com.pragma.order_service.infrastructure.output.postgres.repository.UserBasicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserQueryAdapter implements UserQueryPort {

    private final UserBasicRepository userBasicRepository;
    private final UserBasicEntityMapper userBasicEntityMapper;

    @Override
    public Mono<UserSummary> findById(Long userId) {
        return userBasicRepository.findUserSummaryById(userId)
                .map(userBasicEntityMapper::toDomain);
    }
}
