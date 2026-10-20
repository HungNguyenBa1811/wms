package com.hung.wms.repository.custom;

import java.util.Map;

// Spring Data fragment: InventoryRepository extends this, the code lives in InventoryRepositoryCustomImpl
public interface InventoryRepositoryCustom {
    /**
     * Adds each quantity to the inventory row of (warehouse, product) and creates the row when it does not exist yet,
     * in 1 MySQL statement {@code INSERT ... ON DUPLICATE KEY UPDATE}.
     */
    void addQuantities(String warehouseId, Map<String, Integer> quantityByProductId);
}
