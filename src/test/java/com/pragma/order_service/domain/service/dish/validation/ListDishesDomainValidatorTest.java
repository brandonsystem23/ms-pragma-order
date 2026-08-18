package com.pragma.order_service.domain.service.dish.validation;

import com.pragma.order_service.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListDishesDomainValidatorTest {

    private final ListDishesDomainValidator validator = new ListDishesDomainValidator();

    @Test
    void shouldValidateSuccessfullyWithoutCategory() {
        assertDoesNotThrow(() -> validator.validate(1L, null, 0, 10));
    }

    @Test
    void shouldValidateSuccessfullyWithCategory() {
        assertDoesNotThrow(() -> validator.validate(1L, "PIZZA", 0, 10));
    }

    @Test
    void shouldThrowWhenRestaurantIdIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(null, "PIZZA", 0, 10));

        assertEquals("El campo restaurantId es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPageIsNegative() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, "PIZZA", -1, 10));

        assertEquals("El parámetro page debe ser mayor o igual a 0", ex.getMessage());
    }

    @Test
    void shouldThrowWhenSizeIsZero() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, "PIZZA", 0, 0));

        assertEquals("El parámetro size debe ser mayor a 0", ex.getMessage());
    }

    @Test
    void shouldThrowWhenCategoryIsBlank() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validate(1L, "   ", 0, 10));

        assertEquals("El parámetro category no puede estar vacío", ex.getMessage());
    }
}
