package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignOrderDomainValidatorTest {

    private final AssignOrderDomainValidator validator = new AssignOrderDomainValidator();

    @Test
    void shouldValidateSuccessfullyWhenOrderIdIsValid() {
        assertDoesNotThrow(() -> validator.validate(1L));
    }

    @Test
    void shouldThrowWhenOrderIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(null));

        assertEquals("El campo orderId es obligatorio", ex.getMessage());
    }
}
