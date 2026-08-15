package com.pragma.order_service.infrastructure.output.postgres.mapper;

import com.pragma.order_service.domain.model.UserSummary;
import com.pragma.order_service.infrastructure.output.postgres.entity.UserBasicEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserBasicEntityMapper {

    UserSummary toDomain(UserBasicEntity entity);
}
