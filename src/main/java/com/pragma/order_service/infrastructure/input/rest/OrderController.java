package com.pragma.order_service.infrastructure.input.rest;

import com.pragma.order_service.application.dto.request.CreateOrderRequest;
import com.pragma.order_service.application.dto.request.UpdateOrderRequest;
import com.pragma.order_service.application.dto.response.UpdateOrderResponse;
import com.pragma.order_service.application.dto.response.OrderResponse;
import com.pragma.order_service.application.dto.response.PagedResponse;
import com.pragma.order_service.application.handler.IOrderHandler;
import com.pragma.order_service.infrastructure.util.UtilTokenExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "Endpoints para gestión de pedidos")
public class OrderController {

    private final IOrderHandler iOrderHandler;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear pedido", description = "Crea un pedido para un cliente. Requiere rol CLIENTE")
    public Mono<OrderResponse> create(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CreateOrderRequest request
    ) {
        String token = UtilTokenExtractor.extract(authorizationHeader);

        log.info("Solicitud para crear pedido en restaurante con ID={}", request.restaurantId());

        return iOrderHandler.create(request, token);
    }

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Actualizar estado del pedido",
            description = """
                    Actualiza el estado del pedido según la transición solicitada.
                    Estados soportados:
                    - EN_PREPARACION: asigna el pedido al empleado autenticado
                    - LISTO: marca el pedido como listo
                    - ENTREGADO: marca el pedido como entregado, requiere pin
                    - CANCELADO: cancela el pedido
                    """
    )
    public Mono<UpdateOrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody UpdateOrderRequest request
    ) {
        String token = UtilTokenExtractor.extract(authorizationHeader);

        log.info("Solicitud para cambiar el estado de pedido con ID={}", orderId);

        return iOrderHandler.updateStatus(orderId, request, token);
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
        String token = UtilTokenExtractor.extract(authorizationHeader);

        log.info("Solicitud para listar pedidos con estado {}", status);

        return iOrderHandler.list(token, status, page, size);
    }
}
