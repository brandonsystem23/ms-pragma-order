package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.DeliverOrderRequest;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.service.OrderApplicationService;
import com.pragma.order_service.infrastructure.util.TokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Endpoints para gestión de pedidos")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear pedido", description = "Crea un pedido para un cliente. Requiere rol CLIENTE")
    public Mono<OrderResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CreateOrderRequest request
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return orderApplicationService.create(request, token);
    }

    @PatchMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancelar pedido", description = "Permite cancelar un pedido en estado PENDIENTE. Requiere rol CLIENTE")
    public Mono<OrderResponse> cancel(
            @PathVariable Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return orderApplicationService.cancel(orderId, token);
    }


    @PatchMapping("/{orderId}/assign")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Asignar pedido", description = "Permite asignarse un pedido con estado PENDIENTE. Requiere rol EMPLEADO")
    public Mono<OrderResponse> assign(
            @PathVariable Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return orderApplicationService.assign(orderId, token);
    }

    @PatchMapping("/{orderId}/ready")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Asignar estado LISTO a pedido", description = "Marca un pedido como LISTO y enviar notificacion. Requiere rol EMPLEADO")
    public Mono<OrderResponse> markReady(
            @PathVariable Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return orderApplicationService.markReady(orderId, token);
    }

    @PatchMapping("/{orderId}/deliver")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Entregar pedido", description = "Marca un pedido como ENTREGADO validando PIN de seguridad. Requiere rol EMPLEADO")
    public Mono<OrderResponse> deliver(
            @PathVariable Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody DeliverOrderRequest request
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return orderApplicationService.deliver(orderId, request.pin(), token);
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Listar pedidos", description = "Lista pedidos del restaurante asociado al empleado. Requiere rol EMPLEADO")
    public Mono<PagedResponse<OrderResponse>> list(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(defaultValue = "PENDIENTE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = TokenExtractor.extract(authorizationHeader);
        return orderApplicationService.list(token, status, page, size);
    }
}
