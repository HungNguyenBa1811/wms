package com.hung.wms.dto.response.stockmovement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StockMovementResponse {
    private String id, productId, warehouseId, performedBy, movementType, reason;
    private Integer quantity;
    private LocalDateTime createdAt;
    // Username of the logged-in user who created the row (JPA auditing), null if created without login
    private String createdBy;
}
