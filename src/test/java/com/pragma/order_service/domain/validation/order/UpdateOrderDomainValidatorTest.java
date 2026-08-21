package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateOrderDomainValidatorTest {

    private final UpdateOrderDomainValidator validator = new UpdateOrderDomainValidator();

    @Test
    void shouldValidateSuccessfullyForInPreparation() {
        assertDoesNotThrow(() ->
                validator.validate(1L, new UpdateOrderCommand(OrderStatus.IN_PREPARATION, null)));
    }

    @Test
    void shouldValidateSuccessfullyForReady() {
        assertDoesNotThrow(() ->
                validator.validate(1L, new UpdateOrderCommand(OrderStatus.READY, null)));
    }

    @Test
    void shouldValidateSuccessfullyForDeliveredWithPin() {
        assertDoesNotThrow(() ->
                validator.validate(1L, new UpdateOrderCommand(OrderStatus.DELIVERED, "151370")));
    }

    @Test
    void shouldValidateSuccessfullyForCancelled() {
        assertDoesNotThrow(() ->
                validator.validate(1L, new UpdateOrderCommand(OrderStatus.CANCELLED, null)));
    }

    @Test
    void shouldFailWhenOrderIdIsNull() {

        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.READY, null);

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(null, command));

        assertEquals("El campo orderId es obligatorio", ex.getMessage());
    }

    @Test
    void shouldFailWhenCommandIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, null));

        assertEquals("El campo status es obligatorio", ex.getMessage());
    }


    @Test
    void shouldFailWhenStatusIsNull() {

        UpdateOrderCommand command = new UpdateOrderCommand(null, null);

        DomainException ex = assertThrows(
                DomainException.class,
                () -> validator.validate(1L, command)
        );

        assertEquals("El campo status es obligatorio", ex.getMessage());
    }

    @Test
    void shouldFailWhenStatusIsBlank() {

        UpdateOrderCommand command = new UpdateOrderCommand("   ", null);

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, command));

        assertEquals("El campo status es obligatorio", ex.getMessage());
    }

    @Test
    void shouldFailWhenStatusIsNotSupported() {

        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.PENDING, null);

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, command));

        assertEquals("El estado solicitado no es soportado para actualización", ex.getMessage());
    }

    @Test
    void shouldFailWhenDeliveredAndPinIsMissing() {

        UpdateOrderCommand command = new UpdateOrderCommand(OrderStatus.DELIVERED, null);

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, command));

        assertEquals("El PIN de seguridad es obligatorio", ex.getMessage());
    }
}
