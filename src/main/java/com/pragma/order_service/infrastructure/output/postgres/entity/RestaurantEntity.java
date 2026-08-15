package com.pragma.order_service.infrastructure.output.postgres.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("restaurant")
public class RestaurantEntity {

    @Id
    private Long id;

    private String name;

    private String nit;

    private String address;

    private String phone;

    @Column("url_logo")
    private String urlLogo;

    @Column("owner_id")
    private Long ownerId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
