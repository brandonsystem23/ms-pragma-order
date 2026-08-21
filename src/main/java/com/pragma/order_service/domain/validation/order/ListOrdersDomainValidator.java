package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.validation.ValidationUtils;

import java.util.Set;

public class ListOrdersDomainValidator {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.IN_PREPARATION,
            OrderStatus.CANCELLED,
            OrderStatus.READY,
            OrderStatus.DELIVERED
    );

    public void validate(String status, int page, int size) {
        validateStatus(status);
        validatePage(page);
        validateSize(size);
    }

    private void validateStatus(String status) {
        if (ValidationUtils.isBlank(status)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_INVALID
            );
        }

        if (!ALLOWED_STATUSES.contains(status)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_NOT_FOUND
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
}
