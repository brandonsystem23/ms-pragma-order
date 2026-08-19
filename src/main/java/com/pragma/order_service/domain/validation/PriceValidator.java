package com.pragma.order_service.domain.validation;

import java.math.BigDecimal;

public final class PriceValidator {

    private PriceValidator() {
    }

    public static boolean isInvalid(BigDecimal price) {
        return !isValid(price);
    }

    public static boolean isValid(BigDecimal price) {
        return price != null
                && price.compareTo(BigDecimal.ZERO) > 0
                && price.stripTrailingZeros().scale() <= 0;
    }

}
