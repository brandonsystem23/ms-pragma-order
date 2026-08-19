package com.pragma.order_service.domain.service.dish.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.CreateDishCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DishDomainValidatorTest {

    private final DishDomainValidator validator = new DishDomainValidator();

    @Test
    void shouldValidateSuccessfullyWhenCommandIsValid() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        assertDoesNotThrow(() -> validator.validateForCreate(command));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        CreateDishCommand command = new CreateDishCommand(
                " ",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo name es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                null,
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo price es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceIsZero() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.ZERO,
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El price solo puede ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceIsNegative() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(-1000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El price solo puede ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPriceHasDecimals() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000.5),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El price solo puede ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDescriptionIsBlank() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                " ",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo description es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUrlImageIsBlank() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                " ",
                "PIZZA",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo urlImage es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenCategoryIsBlank() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                " ",
                true,
                1L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo category es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenRestaurantIdIsNull() {
        CreateDishCommand command = new CreateDishCommand(
                "Pizza Hawaiana",
                BigDecimal.valueOf(25000),
                "Pizza con piña y jamón",
                "https://image.com/pizza.png",
                "PIZZA",
                true,
                null
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo restaurantId es obligatorio", ex.getMessage());
    }
}
