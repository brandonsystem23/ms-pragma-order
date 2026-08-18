package com.pragma.order_service.domain.service.restaurant.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;

public class ListRestaurantsDomainValidator {

    public void validate(int page, int size) {
        validatePage(page);
        validateSize(size);
    }


    private void validatePage(int page) {
        if (page < 0) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.PAGE_INVALID
            );
        }
    }

    private void validateSize(int size) {
        if (size <= 0) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.SIZE_INVALID
            );
        }
    }
}
