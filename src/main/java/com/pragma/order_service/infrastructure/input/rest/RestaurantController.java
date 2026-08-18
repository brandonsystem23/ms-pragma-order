package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateRestaurantRequest;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.dto.response.RestaurantListItemResponse;
import com.pragma.order_service.application.dto.response.RestaurantResponse;
import com.pragma.order_service.application.service.RestaurantApplicationService;
import com.pragma.order_service.infrastructure.util.TokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurantes", description = "Endpoints para gestión de restaurantes")
public class RestaurantController {

    private final RestaurantApplicationService restaurantApplicationService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear restaurante", description = "Crea un restaurante asignandolo a un propietario. Requiere rol ADMIN")
    public Mono<RestaurantResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CreateRestaurantRequest request
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return restaurantApplicationService.create(request, token);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar restaurantes", description = "Lista restaurantes activos en orden alfabético y paginados. Requiere rol CLIENTE")
    public Mono<PagedResponse<RestaurantListItemResponse>> list(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return restaurantApplicationService.list(token, page, size);
    }
}
