package com.pragma.order_service.infrastructure.output.postgres.repository;

import com.pragma.order_service.infrastructure.output.postgres.entity.UserBasicEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserBasicRepository extends ReactiveCrudRepository<UserBasicEntity, Long> {

    @Query("""
            SELECT u.id AS id,
                u.first_name AS first_name,
                u.last_name AS last_name,
                u.email AS email,
                u.status AS status,
                r.name AS role_name
            FROM users u
            INNER JOIN role r ON u.role_id = r.id
            WHERE u.id = :userId
            """)
    Mono<UserBasicEntity> findUserSummaryById(Long userId);
}
