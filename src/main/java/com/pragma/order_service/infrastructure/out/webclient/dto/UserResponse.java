package com.pragma.order_service.infrastructure.out.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UserResponse(

        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        Boolean status,
        @JsonProperty("role")
        String roleName
) {
}
