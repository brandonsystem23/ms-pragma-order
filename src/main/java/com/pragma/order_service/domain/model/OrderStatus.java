package com.pragma.order_service.domain.model;

public final class OrderStatus {

    public static final String PENDING = "PENDIENTE";
    public static final String IN_PREPARATION = "EN_PREPARACION";
    public static final String CANCELLED = "CANCELADO";
    public static final String READY = "LISTO";
    public static final String DELIVERED = "ENTREGADO";

    private OrderStatus() {
    }
}
