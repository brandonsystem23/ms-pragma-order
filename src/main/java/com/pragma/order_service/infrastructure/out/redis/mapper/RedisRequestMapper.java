package com.pragma.order_service.infrastructure.out.redis.mapper;

import com.pragma.order_service.domain.model.auth.AuthSession;
import com.pragma.order_service.infrastructure.out.redis.dto.AuthSessionRedisValue;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RedisRequestMapper {

    AuthSession toDomain(AuthSessionRedisValue authSessionRedisValue);

}
