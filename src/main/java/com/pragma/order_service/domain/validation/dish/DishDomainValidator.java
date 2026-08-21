package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.CreateDishCommand;

import com.pragma.order_service.domain.validation.PriceValidator;
import com.pragma.order_service.domain.validation.ValidationUtils;

import java.math.BigDecimal;

public class DishDomainValidator {

    public void validateForCreate(CreateDishCommand command) {
        validateName(command.name());
        validatePrice(command.price());
        validateDescription(command.description());
        validateUrlImage(command.urlImage());
        validateCategory(command.category());
        validateCategory(command.category());
        validateRestaurantId(command.restaurantId());
    }

    private void validateName(String name) {
        if (ValidationUtils.isBlank(name)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NAME_REQUIRED);
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.PRICE_REQUIRED);
        }

        if (PriceValidator.isInvalid(price)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.PRICE_INVALID);
        }
    }

    private void validateDescription(String description) {
        if (ValidationUtils.isBlank(description)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.DESCRIPTION_REQUIRED);
        }
    }


    private void validateUrlImage(String urlImage) {
        if (ValidationUtils.isBlank(urlImage)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.URL_IMAGE_REQUIRED);
        }
    }

    private void validateCategory(String category) {
        if (ValidationUtils.isBlank(category)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.CATEGORY_REQUIRED);
        }
    }

    private void validateRestaurantId(Long restaurantId) {
        if (restaurantId == null) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.RESTAURANT_ID_REQUIRED);
        }
    }
}
