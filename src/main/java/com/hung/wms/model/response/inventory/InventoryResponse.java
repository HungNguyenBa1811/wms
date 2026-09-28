package com.hung.wms.model.response.inventory;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryResponse {
    private String id, warehouseId, productId;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
