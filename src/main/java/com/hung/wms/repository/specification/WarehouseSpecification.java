package com.hung.wms.repository.specification;

import com.hung.wms.model.request.warehouse.WarehouseSearchRequest;
import com.hung.wms.repository.entity.WarehouseEntity;
import org.springframework.data.jpa.domain.Specification;

public class WarehouseSpecification {
    private WarehouseSpecification() {
    }

    public static Specification<WarehouseEntity> search(WarehouseSearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                warehouseCodeContains(request.getWarehouseCode()),
                nameContains(request.getName()),
                locationContains(request.getLocation())
        );
    }

    public static Specification<WarehouseEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<WarehouseEntity> warehouseCodeContains(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(cb.lower(root.get("warehouseCode")), "%" + warehouseCode.trim().toLowerCase() + "%");
    }

    public static Specification<WarehouseEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
    }

    public static Specification<WarehouseEntity> locationContains(String location) {
        if (location == null || location.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(cb.lower(root.get("location")), "%" + location.trim().toLowerCase() + "%");
    }
}
