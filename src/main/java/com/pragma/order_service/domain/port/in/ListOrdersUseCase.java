package com.pragma.order_service.domain.port.in;

import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import reactor.core.publisher.Mono;

public interface ListOrdersUseCase {

    Mono<PageResult<OrderQueryModel>> list(String token, String status, int page, int size);
}
