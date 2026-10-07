package com.hung.wms.model.request.purchaseorder;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderItemRequest {
    @NotBlank(message = "Product is required")
    private String productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Unit cost is required")
    @PositiveOrZero(message = "Unit cost must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Unit cost must have at most 13 integer digits and 2 decimals")
    private BigDecimal unitCost;
}
