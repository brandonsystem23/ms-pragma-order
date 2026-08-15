package com.pragma.order_service.domain.validation;

import java.util.regex.Pattern;

public final class NitValidator {

    private static final Pattern NIT_PATTERN = Pattern.compile("\\d+");

    private NitValidator() {
    }

    public static boolean isValid(String nit) {
        return nit != null && NIT_PATTERN.matcher(nit).matches();
    }
}
