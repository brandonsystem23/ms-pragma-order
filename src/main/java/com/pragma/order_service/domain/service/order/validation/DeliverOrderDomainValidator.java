package com.pragma.order_service.domain.service.order.validation;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.validation.ValidationUtils;

public class DeliverOrderDomainValidator {

    public void validate(Long orderId, String pin) {
        validateOrderId(orderId);
        validatePin(pin);
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ID_REQUIRED
            );
        }
    }

    private void validatePin(String pin) {
        if (ValidationUtils.isBlank(pin)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_DELIVER_PIN_REQUIRED
            );
        }
    }
}
