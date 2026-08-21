package com.pragma.order_service.domain.validation.dish;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.validation.ValidationUtils;

public class ListDishesDomainValidator {

    public void validate(Long restaurantId, String category, int page, int size) {
        validateRestaurantId(restaurantId);
        validatePage(page);
        validateSize(size);
        validateCategory(category);
    }

    private void validateRestaurantId(Long restaurantId) {
        if (restaurantId == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.RESTAURANT_ID_REQUIRED
            );
        }
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

    private void validateCategory(String category) {
        if (category != null && ValidationUtils.isBlank(category)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.CATEGORY_INVALID
            );
        }
    }
}
