package com.pragma.order_service.domain.validation.restaurant;

import com.pragma.order_service.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListRestaurantsDomainValidatorTest {

    private final ListRestaurantsDomainValidator validator = new ListRestaurantsDomainValidator();

    @Test
    void shouldValidateSuccessfullyWithoutCategory() {
        assertDoesNotThrow(() -> validator.validate( 0, 10));
    }

    @Test
    void shouldThrowWhenPageIsNegative() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate( -1, 10));

        assertEquals("El parámetro page debe ser mayor o igual a 0", ex.getMessage());
    }

    @Test
    void shouldThrowWhenSizeIsZero() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate( 0, 0));

        assertEquals("El parámetro size debe ser mayor a 0", ex.getMessage());
    }
}
