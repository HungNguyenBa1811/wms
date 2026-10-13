package com.hung.wms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.charset.StandardCharsets;

public class MaxBytesValidator implements ConstraintValidator<MaxBytes, String> {
    private int max;

    @Override
    public void initialize(MaxBytes constraintAnnotation) {
        max = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null)
            return true;
        return value.getBytes(StandardCharsets.UTF_8).length <= max;
    }
}
