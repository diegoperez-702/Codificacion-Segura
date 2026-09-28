package com.example.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class InputValidator implements ConstraintValidator<ValidInput, String> {
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,10}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return PATTERN.matcher(value).matches();
    }
}
