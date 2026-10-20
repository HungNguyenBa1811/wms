package com.hung.wms.dto.request.product;

import com.hung.wms.validation.OnCreate;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProductRequest {
    @NotBlank(groups = OnCreate.class, message = "Product code is required")
    @Size(max = 255, message = "Product code must be at most 255 characters")
    private String productCode;

    @NotBlank(groups = OnCreate.class, message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @NotBlank(groups = OnCreate.class, message = "Category is required")
    private String categoryId;

    @NotNull(groups = OnCreate.class, message = "Price is required")
    @DecimalMin(value = "0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Price must have at most 13 integer digits and 2 decimals")
    private BigDecimal price;

    // Optional, ISO date (yyyy-MM-dd). Missing on create -> today (set in the service)
    @PastOrPresent(message = "Imported date must not be in the future")
    private LocalDate importedAt;

    // Optional, ISO date (yyyy-MM-dd), null = the product does not expire
    private LocalDate expiryDate;

    // Pure input check when both dates are in the body. A PUT that sends only one of them is compared with the
    // stored value in ProductServiceImpl (needs DB data, so it cannot be a constraint here)
    @AssertTrue(message = "Expiry date must not be before imported date")
    public boolean isExpiryDateValid() {
        return importedAt == null || expiryDate == null || !expiryDate.isBefore(importedAt);
    }
}
