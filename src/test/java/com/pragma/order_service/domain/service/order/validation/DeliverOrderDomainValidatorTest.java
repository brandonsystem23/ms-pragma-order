package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliverOrderDomainValidatorTest {

    private final DeliverOrderDomainValidator validator = new DeliverOrderDomainValidator();

    @Test
    void shouldValidateSuccessfully() {
        assertDoesNotThrow(() -> validator.validate(1L, "151370"));
    }

    @Test
    void shouldFailWhenOrderIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(null, "151370"));

        assertEquals("El campo orderId es obligatorio", ex.getMessage());
    }

    @Test
    void shouldFailWhenPinIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, null));

        assertEquals("El PIN de seguridad es obligatorio", ex.getMessage());
    }

    @Test
    void shouldFailWhenPinIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, "   "));

        assertEquals("El PIN de seguridad es obligatorio", ex.getMessage());
    }
}
