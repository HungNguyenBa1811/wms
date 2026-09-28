package com.hung.wms.model.request.purchaseorder;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseOrderItemRequest {
    private String productId;
    private Integer quantity;
    private BigDecimal unitCost;
}
