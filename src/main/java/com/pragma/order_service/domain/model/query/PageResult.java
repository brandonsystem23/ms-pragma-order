package com.pragma.order_service.domain.model.query;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
