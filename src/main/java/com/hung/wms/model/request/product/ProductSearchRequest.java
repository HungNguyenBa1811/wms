package com.hung.wms.model.request.product;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductSearchRequest {
    private String name, productCode, categoryId;

    @PositiveOrZero(message = "priceFrom must be greater than or equal to 0")
    private BigDecimal priceFrom;

    @PositiveOrZero(message = "priceTo must be greater than or equal to 0")
    private BigDecimal priceTo;

    @AssertTrue(message = "priceFrom must be less than or equal to priceTo")
    public boolean isPriceRangeValid() {
        return priceFrom == null || priceTo == null || priceFrom.compareTo(priceTo) <= 0;
    }
}
