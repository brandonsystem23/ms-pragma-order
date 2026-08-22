package com.pragma.order_service.infrastructure.out.webclient.mapper;

import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import com.pragma.order_service.infrastructure.out.webclient.dto.CreateTraceabilityRequest;
import com.pragma.order_service.infrastructure.out.webclient.dto.TraceabilityResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TraceabilityMapper {

    CreateTraceabilityRequest toRequest(Traceability traceability);

    TraceabilityRecord toDomain(TraceabilityResponse traceabilityResponse);
}
