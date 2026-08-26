package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IDishHandler;
import com.pragma.order_service.infrastructure.security.jwt.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/dish")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Platos", description = "Endpoints para gestión de platos")
public class DishController {

    private final IDishHandler iDishHandler;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear plato", description = "Crea un plato asignándolo a un restaurante. Requiere rol PROPIETARIO")
    public Mono<DishResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody CreateDishRequest request
    ) {
        log.info("Solicitud de creación de plato {} por propietario ID={}",
                request.name(), authenticatedUser.userId());

        return iDishHandler.create(request, authenticatedUser.userId());
    }

    @PutMapping("/{dishId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Modificar plato", description = "Modifica precio y descripción de un plato. Requiere rol PROPIETARIO")
    public Mono<DishResponse> update(
            @PathVariable Long dishId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestBody UpdateDishRequest request
    ) {
        log.info("Solicitud para modificar plato con ID={} por propietario ID={}",
                dishId, authenticatedUser.userId());

        return iDishHandler.update(dishId, request, authenticatedUser.userId());
    }

    @PatchMapping("/{dishId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Habilitar o deshabilitar plato", description = "Cambia el estado de disponibilidad de un plato. Requiere rol PROPIETARIO")
    public Mono<DishResponse> updateStatus(
            @PathVariable Long dishId,
            @RequestParam boolean status,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser
    ) {
        log.info("Solicitud para modificar estado de plato con ID={} por propietario ID={}",
                dishId, authenticatedUser.userId());

        return iDishHandler.updateStatus(dishId, status, authenticatedUser.userId());
    }

    @GetMapping("/restaurant/{restaurantId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar platos", description = "Lista los platos de un restaurante. Requiere rol CLIENTE")
    public Mono<PagedResponse<DishResponse>> listByRestaurant(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Solicitud para listar platos del restaurante con ID={} por usuario ID={}",
                restaurantId, authenticatedUser.userId());

        return iDishHandler.listByRestaurant(restaurantId, category, page, size);
    }
}
