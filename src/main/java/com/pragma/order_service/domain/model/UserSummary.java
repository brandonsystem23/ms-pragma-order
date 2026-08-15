package com.pragma.order_service.domain.model;

import lombok.Builder;

@Builder
public record UserSummary(

        Long id,
        String firstName,
        String lastName,
        String email,
        Boolean status,
        String roleName
) {
}
