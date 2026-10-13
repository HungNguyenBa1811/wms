package com.hung.wms.repository.specification;

import com.hung.wms.model.request.inventory.InventorySearchRequest;
import com.hung.wms.repository.entity.InventoryEntity;
import org.springframework.data.jpa.domain.Specification;

public class InventorySpecification {
    private InventorySpecification() {
    }

    public static Specification<InventoryEntity> search(InventorySearchRequest request) {
        return Specification.allOf(
                hasWarehouse(request.getWarehouseId()),
                hasProduct(request.getProductId())
        );
    }

    public static Specification<InventoryEntity> hasWarehouse(String warehouseId) {
        if (warehouseId == null || warehouseId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("warehouse").get("id"), warehouseId);
    }

    public static Specification<InventoryEntity> hasProduct(String productId) {
        if (productId == null || productId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }
}
