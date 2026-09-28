package com.hung.wms.model.request.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryRequest {
    private String warehouseId, productId;
    private Integer quantity;
}
