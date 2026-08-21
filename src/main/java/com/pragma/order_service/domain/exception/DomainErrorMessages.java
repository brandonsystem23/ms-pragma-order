package com.pragma.order_service.domain.exception;

public final class DomainErrorMessages {

    public static final String NAME_REQUIRED = "El campo name es obligatorio";
    public static final String NAME_INVALID = "El nombre del restaurante no puede contener solo números";

    public static final String NIT_REQUIRED = "El campo nit es obligatorio";
    public static final String NIT_NUMERIC = "El NIT debe contener únicamente números";
    public static final String DUPLICATE_NIT = "El NIT ya está registrado";

    public static final String ADDRESS_REQUIRED = "El campo address es obligatorio";

    public static final String PHONE_REQUIRED = "El campo phoneNumber es obligatorio";
    public static final String PHONE_MAX_LENGTH = "El phoneNumber no puede tener más de 13 caracteres";
    public static final String PHONE_INVALID = "El phoneNumber solo puede contener números y opcionalmente iniciar con +";

    public static final String URL_LOGO_REQUIRED = "El campo urlLogo es obligatorio";

    public static final String OWNER_ID_REQUIRED = "El campo ownerId es obligatorio";
    public static final String OWNER_NOT_FOUND = "El propietario no existe";
    public static final String INVALID_OWNER_ROLE = "El usuario indicado no tiene rol PROPIETARIO";

    public static final String DUPLICATE_NAME = "El nombre del palto ya está registrado";

    public static final String PRICE_REQUIRED = "El campo price es obligatorio";
    public static final String PRICE_INVALID = "El price solo puede ser un número entero positivo";

    public static final String DESCRIPTION_REQUIRED = "El campo description es obligatorio";

    public static final String URL_IMAGE_REQUIRED = "El campo urlImage es obligatorio";

    public static final String CATEGORY_REQUIRED = "El campo category es obligatorio";

    public static final String RESTAURANT_ID_REQUIRED = "El campo restaurantId es obligatorio";
    public static final String RESTAURANT_NOT_FOUND = "El restaurante no existe";
    public static final String INVALID_RESTAURANT = "Usted no es propietario del restaurante";
    public static final String DISH_ACCESS_DENIED = "No tienes permisos para crear platos";
    public static final String DISH_LIST_ACCESS_DENIED = "No tienes permisos para listar platos";

    public static final String INVALID_TOKEN = "Token inválido o expirado";
    public static final String RESTAURANT_ACCESS_DENIED = "No tienes permisos para crear restaurantes";
    public static final String RESTAURANT_LIST_ACCESS_DENIED = "No tienes permisos para listar restaurantes";

    public static final String DISH_NOT_FOUND = "El plato no existe";
    public static final String DISH_ID_REQUIRED = "El campo dishId es obligatorio";
    public static final String DISH_UPDATE_FIELDS_REQUIRED = "Debe enviar al menos uno de los campos: price o description";
    public static final String DISH_STATUS_REQUIRED = "El campo status es obligatorio";

    public static final String PAGE_INVALID = "El parámetro page debe ser mayor o igual a 0";
    public static final String SIZE_INVALID = "El parámetro size debe ser mayor a 0";
    public static final String CATEGORY_INVALID = "El parámetro category no puede estar vacío";

    public static final String ORDER_ID_REQUIRED = "El campo orderId es obligatorio";
    public static final String ORDER_NOT_FOUND = "El pedido no existe";
    public static final String ORDER_ITEMS_REQUIRED = "La lista de platos es obligatoria";
    public static final String ORDER_ITEM_INVALID = "Cada item del pedido debe ser válido";
    public static final String ORDER_ITEM_QUANTITY_REQUIRED = "La cantidad del plato es obligatoria";
    public static final String ORDER_ITEM_QUANTITY_INVALID = "La cantidad del plato debe ser un número entero positivo";
    public static final String ORDER_CREATE_ACCESS_DENIED = "No tienes permisos para crear pedidos";
    public static final String ACTIVE_ORDER_EXISTS = "El cliente ya tiene un pedido en proceso para este restaurante";
    public static final String ORDER_DISH_INVALID_RESTAURANT = "Todos los platos del pedido deben pertenecer al restaurante indicado";
    public static final String ORDER_ASSIGN_ACCESS_DENIED = "No tienes permisos para asignarte pedidos";
    public static final String ORDER_ASSIGN_INVALID_STATUS = "Solo se pueden asignar pedidos en estado PENDIENTE";
    public static final String ORDER_ALREADY_ASSIGNED = "El pedido ya tiene un empleado asignado";
    public static final String ORDER_ASSIGN_DIFFERENT_RESTAURANT = "No puedes asignarte pedidos de otro restaurante";
    public static final String ORDER_READY_ACCESS_DENIED = "No tienes permisos para marcar pedidos como listos";
    public static final String ORDER_READY_INVALID_STATUS = "Solo se pueden marcar como listos pedidos en estado EN_PREPARACION";
    public static final String ORDER_READY_NOT_ASSIGNED_EMPLOYEE = "No puedes marcar como listo un pedido que no tienes asignado";
    public static final String ORDER_DELIVER_ACCESS_DENIED = "No tienes permisos para entregar pedidos";
    public static final String ORDER_DELIVER_INVALID_STATUS = "Solo se pueden marcar como entregados pedidos en estado LISTO";
    public static final String ORDER_DELIVER_PIN_REQUIRED = "El PIN de seguridad es obligatorio";
    public static final String ORDER_DELIVER_INVALID_PIN = "El PIN de seguridad es inválido";
    public static final String ORDER_DELIVER_NOT_ASSIGNED_EMPLOYEE = "No puedes entregar un pedido que no tienes asignado";
    public static final String ORDER_CANCEL_ACCESS_DENIED = "No tienes permisos para cancelar pedidos";
    public static final String ORDER_CANCEL_NOT_CREATED = "No puedes cancelar un pedido de otro cliente";
    public static final String ORDER_CANCEL_INVALID_STATUS = "Lo sentimos, tu pedido ya está en preparación y no puede cancelarse";


    public static final String ORDER_LIST_ACCESS_DENIED = "No tienes permisos para listar pedidos";
    public static final String ORDER_STATUS_INVALID = "El parámetro status es obligatorio";
    public static final String ORDER_STATUS_NOT_FOUND = "El estado ingresado no existe";
    public static final String EMPLOYEE_RESTAURANT_NOT_FOUND = "El empleado no tiene un restaurante asignado";

    public static final String ORDER_STATUS_UPDATE_REQUIRED = "El campo status es obligatorio";
    public static final String ORDER_STATUS_UPDATE_NOT_SUPPORTED = "El estado solicitado no es soportado para actualización";


    private DomainErrorMessages() {
    }
}
