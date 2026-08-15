package com.pragma.order_service.domain.validation;

import java.util.regex.Pattern;

public final class PhoneValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?\\d+$");
    private static final int MAX_LENGTH = 13;

    private PhoneValidator() {
    }

    public static boolean hasValidFormat(String phone) {
        return phone != null
                && phone.length() <= MAX_LENGTH
                && PHONE_PATTERN.matcher(phone).matches();
    }
}
