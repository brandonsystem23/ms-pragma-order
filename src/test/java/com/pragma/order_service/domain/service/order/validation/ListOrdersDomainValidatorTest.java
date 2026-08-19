package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListOrdersDomainValidatorTest {

    private final ListOrdersDomainValidator validator = new ListOrdersDomainValidator();

    @Test
    void shouldValidateSuccessfullyWithPendingStatus() {
        assertDoesNotThrow(() -> validator.validate(OrderStatus.PENDING, 0, 10));
    }

    @Test
    void shouldValidateSuccessfullyWithInPreparationStatus() {
        assertDoesNotThrow(() -> validator.validate(OrderStatus.IN_PREPARATION, 0, 10));
    }

    @Test
    void shouldValidateSuccessfullyWithCancelledStatus() {
        assertDoesNotThrow(() -> validator.validate(OrderStatus.CANCELLED, 0, 10));
    }

    @Test
    void shouldValidateSuccessfullyWithReadyStatus() {
        assertDoesNotThrow(() -> validator.validate(OrderStatus.READY, 0, 10));
    }

    @Test
    void shouldValidateSuccessfullyWithDeliveredStatus() {
        assertDoesNotThrow(() -> validator.validate(OrderStatus.DELIVERED, 0, 10));
    }

    @Test
    void shouldThrowWhenStatusIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(null, 0, 10));

        assertEquals("El parámetro status es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenStatusIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate("   ", 0, 10));

        assertEquals("El parámetro status es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenStatusDoesNotExist() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate("FINALIZADO", 0, 10));

        assertEquals("El estado ingresado no existe", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPageIsNegative() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(OrderStatus.PENDING, -1, 10));

        assertEquals("El parámetro page debe ser mayor o igual a 0", ex.getMessage());
    }

    @Test
    void shouldThrowWhenSizeIsZero() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(OrderStatus.PENDING, 0, 0));

        assertEquals("El parámetro size debe ser mayor a 0", ex.getMessage());
    }
}
