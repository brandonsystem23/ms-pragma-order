package com.pragma.order_service.domain.service.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.UpdateDishCommand;
import com.pragma.order_service.domain.validation.PriceValidator;
import com.pragma.order_service.domain.validation.ValidationUtils;

import java.math.BigDecimal;

public class UpdateDishDomainValidator {

    public void validateForUpdate(Long dishId, UpdateDishCommand command) {
        validateDishId(dishId);
        validateAtLeastOneField(command);

        if (command.price() != null) {
            validatePrice(command.price());
        }

        if (command.description() != null) {
            validateDescription(command.description());
        }
    }

    private void validateDishId(Long dishId) {
        if (dishId == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.DISH_ID_REQUIRED
            );
        }
    }

    private void validateAtLeastOneField(UpdateDishCommand command) {
        boolean priceMissing = command.price() == null;
        boolean descriptionMissing = ValidationUtils.isBlank(command.description());

        if (priceMissing && descriptionMissing) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.DISH_UPDATE_FIELDS_REQUIRED
            );
        }
    }

    private void validatePrice(BigDecimal price) {
        if (!PriceValidator.isValid(price)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.PRICE_INVALID
            );
        }
    }

    private void validateDescription(String description) {
        if (ValidationUtils.isBlank(description)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.DESCRIPTION_REQUIRED
            );
        }
    }
}
