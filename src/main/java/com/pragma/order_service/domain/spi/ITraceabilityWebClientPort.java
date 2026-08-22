package com.pragma.order_service.domain.spi;

import com.pragma.order_service.domain.model.Traceability;
import com.pragma.order_service.domain.model.TraceabilityRecord;
import reactor.core.publisher.Mono;

public interface ITraceabilityWebClientPort {

    Mono<TraceabilityRecord> create(Traceability traceability, String token);
}
