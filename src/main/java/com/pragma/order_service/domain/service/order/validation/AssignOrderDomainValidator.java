package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;

public class AssignOrderDomainValidator {

    public void validate(Long orderId) {
        if (orderId == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ID_REQUIRED
            );
        }
    }
}
