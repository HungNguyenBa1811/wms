package com.hung.wms.dto.request.inventory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventorySearchRequest {
    private String warehouseId, productId;
}
