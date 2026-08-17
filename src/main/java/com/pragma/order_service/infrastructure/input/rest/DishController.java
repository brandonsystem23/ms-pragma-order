package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateDishRequest;
import com.pragma.order_service.application.dto.request.UpdateDishRequest;
import com.pragma.order_service.application.dto.response.DishResponse;
import com.pragma.order_service.application.service.DishApplicationService;
import com.pragma.order_service.infrastructure.util.TokenExtractor;
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

    private final DishApplicationService dishApplicationService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear plato", description = "Crea un plato asignandolo a un restaurante. Requiere rol PROPIETARIO")
    public Mono<DishResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CreateDishRequest request
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return dishApplicationService.create(request, token);
    }

    @PutMapping("/{dishId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Modificar plato", description = "Modifica precio y descripción de un plato. Requiere rol PROPIETARIO")
    public Mono<DishResponse> update(
            @PathVariable Long dishId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody UpdateDishRequest request
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return dishApplicationService.update(dishId, request, token);
    }
}
