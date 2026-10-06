package com.hung.wms.model.request.product;

import com.hung.wms.validation.OnCreate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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
}
