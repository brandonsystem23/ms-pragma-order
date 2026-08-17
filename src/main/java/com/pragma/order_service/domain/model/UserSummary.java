package com.pragma.order_service.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UserSummary(

        Long id,
        String firstName,
        String lastName,
        String email,
        Boolean status,
        @JsonProperty("role")
        String roleName
) {
}
