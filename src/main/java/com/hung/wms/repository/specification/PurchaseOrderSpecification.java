package com.hung.wms.repository.specification;

import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderSearchRequest;
import com.hung.wms.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.domain.Specification;

public class PurchaseOrderSpecification {
    private PurchaseOrderSpecification() {
    }

    public static Specification<PurchaseOrderEntity> search(PurchaseOrderSearchRequest request) {
        return Specification.allOf(
                hasStatus(request.getStatus()),
                hasSupplier(request.getSupplierId()),
                hasWarehouse(request.getWarehouseId())
        );
    }

    public static Specification<PurchaseOrderEntity> hasStatus(PurchaseOrderStatus status) {
        if (status == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<PurchaseOrderEntity> hasSupplier(String supplierId) {
        if (supplierId == null || supplierId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("supplier").get("id"), supplierId);
    }

    public static Specification<PurchaseOrderEntity> hasWarehouse(String warehouseId) {
        if (warehouseId == null || warehouseId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("warehouse").get("id"), warehouseId);
    }
}
