package com.hung.wms.dto.request.stockmovement;

import com.hung.wms.enums.MovementType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockMovementSearchRequest {
    private String warehouseId, productId;
    private MovementType movementType;
}
