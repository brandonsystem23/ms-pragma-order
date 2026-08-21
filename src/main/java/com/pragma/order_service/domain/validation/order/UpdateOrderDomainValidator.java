package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.OrderStatus;
import com.pragma.order_service.domain.model.command.UpdateOrderCommand;
import com.pragma.order_service.domain.validation.ValidationUtils;

import java.util.Set;

public class UpdateOrderDomainValidator {

    private static final Set<String> ALLOWED_TARGET_STATUSES = Set.of(
            OrderStatus.IN_PREPARATION,
            OrderStatus.READY,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED
    );

    public void validate(Long orderId, UpdateOrderCommand command) {
        validateOrderId(orderId);
        validateCommand(command);
        validateStatus(command.status());
        validatePinIfRequired(command.status(), command.pin());
    }

    private void validateOrderId(Long orderId) {
        if (orderId == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ID_REQUIRED
            );
        }
    }

    private void validateCommand(UpdateOrderCommand command) {
        if (command == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_UPDATE_REQUIRED
            );
        }
    }

    private void validateStatus(String status) {
        if (ValidationUtils.isBlank(status)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_UPDATE_REQUIRED
            );
        }

        if (!ALLOWED_TARGET_STATUSES.contains(status)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_STATUS_UPDATE_NOT_SUPPORTED
            );
        }
    }

    private void validatePinIfRequired(String status, String pin) {
        if (OrderStatus.DELIVERED.equals(status) && ValidationUtils.isBlank(pin)) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_DELIVER_PIN_REQUIRED
            );
        }
    }
}
