package com.hung.wms.dto.response.purchaseorder;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PurchaseOrderItemResponse {
    private String id, productId;
    private Integer quantity;
    private BigDecimal unitCost;
    private LocalDateTime createdAt;
    // Username of the logged-in user who created the row (JPA auditing), null if created without login
    private String createdBy;
}
