package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDomainValidatorTest {

    private final OrderDomainValidator validator = new OrderDomainValidator();

    @Test
    void shouldValidateSuccessfullyWhenCommandIsValid() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(
                        new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)),
                        new CreateOrderItemCommand(11L, BigDecimal.ONE)
                )
        );

        assertDoesNotThrow(() -> validator.validateForCreate(command));
    }

    @Test
    void shouldThrowWhenRestaurantIdIsNull() {
        CreateOrderCommand command = new CreateOrderCommand(
                null,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(2)))
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo restaurantId es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenItemsAreNull() {
        CreateOrderCommand command = new CreateOrderCommand(1L, null);

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("La lista de platos es obligatoria", ex.getMessage());
    }

    @Test
    void shouldThrowWhenItemsAreEmpty() {
        CreateOrderCommand command = new CreateOrderCommand(1L, List.of());

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("La lista de platos es obligatoria", ex.getMessage());
    }

    @Test
    void shouldThrowWhenItemIsNull() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                Collections.singletonList(null)
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("Cada item del pedido debe ser válido", ex.getMessage());
    }

    @Test
    void shouldThrowWhenDishIdIsNull() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(null, BigDecimal.valueOf(2)))
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo dishId es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenQuantityIsNull() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, null))
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("La cantidad del plato es obligatoria", ex.getMessage());
    }

    @Test
    void shouldThrowWhenQuantityIsZero() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.ZERO))
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("La cantidad del plato debe ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenQuantityIsNegative() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(-1)))
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("La cantidad del plato debe ser un número entero positivo", ex.getMessage());
    }

    @Test
    void shouldThrowWhenQuantityHasDecimals() {
        CreateOrderCommand command = new CreateOrderCommand(
                1L,
                List.of(new CreateOrderItemCommand(10L, BigDecimal.valueOf(1.5)))
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("La cantidad del plato debe ser un número entero positivo", ex.getMessage());
    }
}
