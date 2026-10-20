package com.hung.wms.dto.response.inventory;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InventoryResponse {
    private String id, warehouseId, productId;
    private Integer quantity;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    // Username of the logged-in user who created the row (JPA auditing), null if created without login
    private String createdBy;
}
