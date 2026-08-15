package com.pragma.order_service.domain.service.restaurant;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.CreateRestaurantCommand;
import com.pragma.order_service.domain.validation.NitValidator;
import com.pragma.order_service.domain.validation.PhoneValidator;
import com.pragma.order_service.domain.validation.RestaurantNameValidator;
import com.pragma.order_service.domain.validation.ValidationUtils;

public class RestaurantDomainValidator {

    public void validateForCreate(CreateRestaurantCommand command) {
        validateName(command.name());
        validateNit(command.nit());
        validateAddress(command.address());
        validatePhone(command.phone());
        validateUrlLogo(command.urlLogo());
        validateOwnerId(command.ownerId());
    }

    private void validateName(String name) {
        if (ValidationUtils.isBlank(name)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NAME_REQUIRED);
        }

        if (!RestaurantNameValidator.isValid(name)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NAME_INVALID);
        }
    }

    private void validateNit(String nit) {
        if (ValidationUtils.isBlank(nit)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NIT_REQUIRED);
        }

        if (!NitValidator.isValid(nit)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.NIT_NUMERIC);
        }
    }

    private void validateAddress(String address) {
        if (ValidationUtils.isBlank(address)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.ADDRESS_REQUIRED);
        }
    }

    private void validatePhone(String phone) {
        if (ValidationUtils.isBlank(phone)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.PHONE_REQUIRED);
        }

        if (phone.length() > 13) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.PHONE_MAX_LENGTH);
        }

        if (!PhoneValidator.hasValidFormat(phone)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.PHONE_INVALID);
        }
    }

    private void validateUrlLogo(String urlLogo) {
        if (ValidationUtils.isBlank(urlLogo)) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.URL_LOGO_REQUIRED);
        }
    }

    private void validateOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.OWNER_ID_REQUIRED);
        }
    }
}
