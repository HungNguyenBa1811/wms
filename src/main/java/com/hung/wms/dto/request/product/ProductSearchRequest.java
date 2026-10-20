package com.hung.wms.dto.request.product;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProductSearchRequest {
    private String name, productCode, categoryId;

    @PositiveOrZero(message = "priceFrom must be greater than or equal to 0")
    private BigDecimal priceFrom;

    @PositiveOrZero(message = "priceTo must be greater than or equal to 0")
    private BigDecimal priceTo;

    // Products expiring strictly before this date (yyyy-MM-dd), e.g. to find goods about to expire.
    // Products without expiryDate never match
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryBefore;

    @AssertTrue(message = "priceFrom must be less than or equal to priceTo")
    public boolean isPriceRange() {
        return priceFrom == null || priceTo == null || priceFrom.compareTo(priceTo) <= 0;
    }
}
