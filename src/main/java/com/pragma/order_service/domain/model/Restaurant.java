package com.pragma.order_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    private Long id;

    private String name;

    private String nit;

    private String address;

    private String phone;

    private String urlLogo;

    private Long ownerId;

    private Boolean status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
