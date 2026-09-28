package com.hung.wms.model.request.stockmovement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementRequest {
    private String productId, warehouseId, performedBy, movementType, reason;
    private Integer quantity;
}
