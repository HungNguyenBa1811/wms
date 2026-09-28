package com.hung.wms.model.response.product;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductResponse {
    private String id, productCode, name, description, categoryId;
    private BigDecimal price;
}
