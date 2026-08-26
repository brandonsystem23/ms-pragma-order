package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.handler.IRestaurantHandler;
import com.pragma.order_service.infrastructure.security.jwt.AuthenticatedUser;
import com.pragma.order_service.infrastructure.util.UtilTokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Restaurantes", description = "Endpoints para gestión de restaurantes")
public class RestaurantController {

    private final IRestaurantHandler iRestaurantHandler;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear restaurante", description = "Crea un restaurante asignándolo a un propietario. Requiere rol ADMINISTRADOR")
    public Mono<RestaurantResponse> create(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CreateRestaurantRequest request
    ) {
        log.info("Solicitud para crear el restaurante {} al propietario con ID={} por usuario autenticado ID={}",
                request.name(), request.ownerId(), authenticatedUser.userId());

        String token = UtilTokenExtractor.extract(authorizationHeader);

        return iRestaurantHandler.create(request, token);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar restaurantes", description = "Lista restaurantes activos en orden alfabético y paginados. Requiere rol CLIENTE")
    public Mono<PagedResponse<RestaurantListResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Solicitud para listar restaurantes por usuario autenticado ID={}", authenticatedUser.userId());
        return iRestaurantHandler.list(page, size);
    }
}
