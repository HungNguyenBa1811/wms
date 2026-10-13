package com.hung.wms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated String must encode to at most {@code value} bytes in UTF-8.
 * Unlike @Size (which counts characters), this matches encoders that count bytes,
 * e.g. BCrypt only accepts 72 bytes: 40 Vietnamese characters can exceed that and crash the encoder.
 * Null is valid (combine with @NotBlank for required fields).
 */
@Documented
@Constraint(validatedBy = MaxBytesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxBytes {
    int value();

    String message() default "must be at most {value} bytes in UTF-8";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
