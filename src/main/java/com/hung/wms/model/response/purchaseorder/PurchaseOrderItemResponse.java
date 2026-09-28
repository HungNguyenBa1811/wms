package com.hung.wms.model.response.purchaseorder;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderItemResponse {
    private String id, productId;
    private Integer quantity;
    private BigDecimal unitCost;
}
