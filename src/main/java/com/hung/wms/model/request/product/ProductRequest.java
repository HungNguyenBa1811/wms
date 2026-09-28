package com.hung.wms.model.request.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {
    private String productCode, name, description, categoryId;
    private BigDecimal price;
}
