package com.hung.wms.model.request.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventorySearchRequest {
    private String warehouseId, productId;
}
