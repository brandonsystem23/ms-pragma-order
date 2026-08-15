package com.pragma.order_service.domain.exception;

public final class DomainErrorMessages {

    public static final String NAME_REQUIRED = "El campo name es obligatorio";
    public static final String NAME_INVALID = "El nombre del restaurante no puede contener solo números";

    public static final String NIT_REQUIRED = "El campo nit es obligatorio";
    public static final String NIT_NUMERIC = "El NIT debe contener únicamente números";
    public static final String DUPLICATE_NIT = "El NIT ya está registrado";

    public static final String ADDRESS_REQUIRED = "El campo address es obligatorio";

    public static final String PHONE_REQUIRED = "El campo phone es obligatorio";
    public static final String PHONE_MAX_LENGTH = "El phone no puede tener más de 13 caracteres";
    public static final String PHONE_INVALID = "El phone solo puede contener números y opcionalmente iniciar con +";

    public static final String URL_LOGO_REQUIRED = "El campo urlLogo es obligatorio";

    public static final String OWNER_ID_REQUIRED = "El campo ownerId es obligatorio";
    public static final String OWNER_NOT_FOUND = "El propietario no existe";
    public static final String INVALID_OWNER_ROLE = "El usuario indicado no tiene rol PROPIETARIO";

    public static final String INVALID_TOKEN = "Token inválido o expirado";
    public static final String ACCESS_DENIED = "No tienes permisos para crear restaurantes";

    private DomainErrorMessages() {
    }
}
