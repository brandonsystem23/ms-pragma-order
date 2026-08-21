package com.pragma.order_service.domain.api;

import com.pragma.order_service.domain.model.query.OrderQueryModel;
import com.pragma.order_service.domain.model.query.PageResult;
import reactor.core.publisher.Mono;

public interface IListOrdersServicePort {

    Mono<PageResult<OrderQueryModel>> list(String token, String status, int page, int size);
}
