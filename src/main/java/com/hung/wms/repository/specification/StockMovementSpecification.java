package com.hung.wms.repository.specification;

import com.hung.wms.enums.MovementType;
import com.hung.wms.dto.request.stockmovement.StockMovementSearchRequest;
import com.hung.wms.entity.StockMovementEntity;
import org.springframework.data.jpa.domain.Specification;

public class StockMovementSpecification {
    private StockMovementSpecification() {
    }

    public static Specification<StockMovementEntity> search(StockMovementSearchRequest request) {
        return Specification.allOf(
                hasWarehouse(request.getWarehouseId()),
                hasProduct(request.getProductId()),
                hasMovementType(request.getMovementType())
        );
    }

    public static Specification<StockMovementEntity> hasWarehouse(String warehouseId) {
        if (warehouseId == null || warehouseId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("warehouse").get("id"), warehouseId);
    }

    public static Specification<StockMovementEntity> hasProduct(String productId) {
        if (productId == null || productId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }

    public static Specification<StockMovementEntity> hasMovementType(MovementType movementType) {
        if (movementType == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("movementType"), movementType);
    }
}
