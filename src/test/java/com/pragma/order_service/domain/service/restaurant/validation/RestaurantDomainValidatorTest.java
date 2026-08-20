package com.pragma.order_service.domain.service.restaurant.validation;

import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantDomainValidatorTest {

    private final RestaurantDomainValidator validator = new RestaurantDomainValidator();

    @Test
    void shouldValidateSuccessfullyWhenCommandIsValid() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante La 70",
                "123456789",
                "Calle 10 # 20-30",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        assertDoesNotThrow(() -> validator.validateForCreate(command));
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                " ",
                "123456789",
                "Calle 10",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo name es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenNameContainsOnlyNumbers() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "123456",
                "123456789",
                "Calle 10",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El nombre del restaurante no puede contener solo números", ex.getMessage());
    }

    @Test
    void shouldThrowWhenNitIsBlank() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                " ",
                "Calle 10",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo nit es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenNitIsNotNumeric() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "ABC123",
                "Calle 10",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El NIT debe contener únicamente números", ex.getMessage());
    }

    @Test
    void shouldThrowWhenAddressIsBlank() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "123456789",
                " ",
                "+573005698325",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo address es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPhoneIsBlank() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "123456789",
                "Calle 10",
                " ",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo phoneNumber es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPhoneLengthIsGreaterThan13() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "123456789",
                "Calle 10",
                "+5730056983259",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El phoneNumber no puede tener más de 13 caracteres", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPhoneFormatIsInvalid() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "123456789",
                "Calle 10",
                "300-5698325",
                "https://logo.com/logo.png",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El phoneNumber solo puede contener números y opcionalmente iniciar con +", ex.getMessage());
    }

    @Test
    void shouldThrowWhenUrlLogoIsBlank() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "123456789",
                "Calle 10",
                "+573005698325",
                " ",
                2L
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo urlLogo es obligatorio", ex.getMessage());
    }

    @Test
    void shouldThrowWhenOwnerIdIsNull() {
        CreateRestaurantCommand command = new CreateRestaurantCommand(
                "Restaurante",
                "123456789",
                "Calle 10",
                "+573005698325",
                "https://logo.com/logo.png",
                null
        );

        DomainException ex = assertThrows(DomainException.class, () -> validator.validateForCreate(command));
        assertEquals("El campo ownerId es obligatorio", ex.getMessage());
    }
}
