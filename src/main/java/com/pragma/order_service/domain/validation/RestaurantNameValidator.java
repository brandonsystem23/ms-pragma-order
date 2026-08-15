package com.pragma.order_service.domain.validation;

import java.util.regex.Pattern;

public final class RestaurantNameValidator {

    private static final Pattern ONLY_NUMBERS_PATTERN = Pattern.compile("^\\d+$");

    private RestaurantNameValidator() {
    }

    public static boolean isValid(String name) {
        return name != null
                && !name.trim().isEmpty()
                && !ONLY_NUMBERS_PATTERN.matcher(name.trim()).matches();
    }
}
