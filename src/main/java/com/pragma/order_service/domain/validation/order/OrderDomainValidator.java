package com.pragma.order_service.domain.validation.order;

import com.pragma.order_service.domain.exception.DomainErrorCode;
import com.pragma.order_service.domain.exception.DomainErrorMessages;
import com.pragma.order_service.domain.exception.DomainException;
import com.pragma.order_service.domain.model.command.CreateOrderCommand;
import com.pragma.order_service.domain.model.command.CreateOrderItemCommand;
import com.pragma.order_service.domain.validation.PriceValidator;

import java.util.List;

public class OrderDomainValidator {

    public void validateForCreate(CreateOrderCommand command) {
        validateRestaurantId(command.restaurantId());
        validateItems(command.items());
    }

    private void validateRestaurantId(Long restaurantId) {
        if (restaurantId == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.RESTAURANT_ID_REQUIRED
            );
        }
    }

    private void validateItems(List<CreateOrderItemCommand> items) {
        if (items == null || items.isEmpty()) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ITEMS_REQUIRED
            );
        }

        for (CreateOrderItemCommand item : items) {
            validateItem(item);
        }
    }

    private void validateItem(CreateOrderItemCommand item) {
        if (item == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ITEM_INVALID
            );
        }

        if (item.dishId() == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.DISH_ID_REQUIRED
            );
        }

        if (item.quantity() == null) {
            throw new DomainException(
                    DomainErrorCode.VALIDATION_ERROR,
                    DomainErrorMessages.ORDER_ITEM_QUANTITY_REQUIRED
            );
        }

        if (PriceValidator.isInvalid(item.quantity())) {
            throw new DomainException(DomainErrorCode.VALIDATION_ERROR, DomainErrorMessages.ORDER_ITEM_QUANTITY_INVALID);
        }
    }
}
