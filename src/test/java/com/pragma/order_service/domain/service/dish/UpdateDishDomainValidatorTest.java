package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UpdateDishDomainValidatorTest {

    private final UpdateDishDomainValidator validator = new UpdateDishDomainValidator();

    @Test
    void shouldValidateSuccessfullyWhenPriceAndDescriptionAreValid() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(30000),
                "Nueva descripción"
        );

        assertDoesNotThrow(() -> validator.validateForUpdate(1L, command));
    }

    @Test
    void shouldValidateSuccessfullyWhenOnlyPriceIsSent() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(30000),
                null
        );

        assertDoesNotThrow(() -> validator.validateForUpdate(1L, command));
    }

    @Test
    void shouldValidateSuccessfullyWhenOnlyDescriptionIsSent() {
        UpdateDishCommand command = new UpdateDishCommand(
                null,
                "Nueva descripción"
        );

        assertDoesNotThrow(() -> validator.validateForUpdate(1L, command));
    }

    @Test
    void shouldThrowWhenDishIdIsNull() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(30000),
                "Nueva descripción"
        );

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdate(null, command));

        assertEquals("El campo dishId es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenBothFieldsAreMissing() {
        UpdateDishCommand command = new UpdateDishCommand(
                null,
                null
        );

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdate(1L, command));

        assertEquals("Debe enviar al menos uno de los campos: price o description", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDescriptionIsBlankAndPriceIsNull() {
        UpdateDishCommand command = new UpdateDishCommand(
                null,
                "   "
        );

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdate(1L, command));

        assertEquals("Debe enviar al menos uno de los campos: price o description", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceIsZero() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.ZERO,
                null
        );

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdate(1L, command));

        assertEquals("El price solo puede ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceHasDecimals() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(25000.5),
                null
        );

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdate(1L, command));

        assertEquals("El price solo puede ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDescriptionIsBlankAndPriceExists() {
        UpdateDishCommand command = new UpdateDishCommand(
                BigDecimal.valueOf(20000),
                " "
        );

        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdate(1L, command));

        assertEquals("El campo description es obligatorio", ex.getMessage());
    }

    @Test
    void shouldValidateSuccessfullyWhenStatusIsTrue() {
        assertDoesNotThrow(() -> validator.validateForUpdateStatus(1L, true));
    }

    @Test
    void shouldValidateSuccessfullyWhenStatusIsFalse() {
        assertDoesNotThrow(() -> validator.validateForUpdateStatus(1L, false));
    }

    @Test
    void shouldThrowWhenStatusIsNull() {
        DomainException ex = assertThrows(DomainException.class,
                () -> validator.validateForUpdateStatus(1L, null));

        assertEquals("El campo status es obligatorio", ex.getMessage());
    }
}
