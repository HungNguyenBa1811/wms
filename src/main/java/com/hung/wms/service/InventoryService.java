package com.hung.wms.service;

import com.hung.wms.model.response.inventory.InventoryResponse;

import java.util.List;

public interface InventoryService {
    List<InventoryResponse> findAllInventories(String warehouseId, String productId);
}
