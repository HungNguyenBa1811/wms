package com.hung.wms.validation;

import com.hung.wms.dto.request.purchaseorder.PurchaseOrderItemRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueProductIdsValidator implements ConstraintValidator<UniqueProductIds, List<PurchaseOrderItemRequest>> {
    @Override
    public boolean isValid(List<PurchaseOrderItemRequest> items, ConstraintValidatorContext context) {
        if (items == null)
            return true;
        Set<String> seen = new HashSet<>();
        for (PurchaseOrderItemRequest item : items) {
            if (item == null || item.getProductId() == null || item.getProductId().isBlank())
                continue;
            if (!seen.add(item.getProductId()))
                return false;
        }
        return true;
    }
}
