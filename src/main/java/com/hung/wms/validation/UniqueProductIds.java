package com.hung.wms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated list of purchase order items must not contain the same productId twice.
 * Pure input check (no DB needed), so it runs in the controller with the other Bean Validation rules
 * and returns 400 with a field detail ("items: ..."). A duplicate with an item already saved in the PO
 * (POST /purchase-orders/{id}/items) needs DB data and stays in PurchaseOrderServiceImpl.
 * Null list / null items / blank productIds are ignored here (@NotEmpty, @NotNull, @NotBlank report them).
 */
@Documented
@Constraint(validatedBy = UniqueProductIdsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueProductIds {
    String message() default "Each product can appear only once in a purchase order";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
