package com.hung.wms.model.response.stockmovement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StockMovementResponse {
    private String id, productId, warehouseId, performedBy, movementType, reason;
    private Integer quantity;
    private LocalDateTime createdAt;
}
