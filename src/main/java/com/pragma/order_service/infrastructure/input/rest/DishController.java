package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IDishHandler;
import com.pragma.order_service.infrastructure.util.UtilTokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/dish")
@RequiredArgsConstructor
@Tag(name = "Platos", description = "Endpoints para gestión de platos")
public class DishController {

    private final IDishHandler iDishHandler;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear plato", description = "Crea un plato asignandolo a un restaurante. Requiere rol PROPIETARIO")
    public Mono<DishResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CreateDishRequest request
    ) {
        String token = UtilTokenExtractor.extract(authorizationHeader);
        return iDishHandler.create(request, token);
    }

    @PutMapping("/{dishId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Modificar plato", description = "Modifica precio y descripción de un plato. Requiere rol PROPIETARIO")
    public Mono<DishResponse> update(
            @PathVariable Long dishId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody UpdateDishRequest request
    ) {
        String token = UtilTokenExtractor.extract(authorizationHeader);
        return iDishHandler.update(dishId, request, token);
    }

    @PatchMapping("/{dishId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Habilitar o deshabilitar plato", description = "Cambia el estado de disponibilidad de un plato. Requiere rol PROPIETARIO")
    public Mono<DishResponse> updateStatus(
            @PathVariable Long dishId,
            @RequestParam boolean status,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = UtilTokenExtractor.extract(authorizationHeader);
        return iDishHandler.updateStatus(dishId, status, token);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar platos", description = "Lista los platos de un restaurante. Requiere rol CLIENTE")
    public Mono<PagedResponse<DishResponse>> listByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = UtilTokenExtractor.extract(authorizationHeader);
        return iDishHandler.listByRestaurant(restaurantId, category, page, size, token);
    }
}
