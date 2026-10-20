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
                warehouseCodeStartsWith(request.getWarehouseCode()),
                nameContains(request.getName()),
                locationContains(request.getLocation())
        );
    }

    public static Specification<WarehouseEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    // Prefix search: "WH" matches "WH-001" but "001" does not. Uses the unique index on the code column
    public static Specification<WarehouseEntity> warehouseCodeStartsWith(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("warehouseCode"), LikePattern.startsWith(warehouseCode), LikePattern.ESCAPE);
    }

    public static Specification<WarehouseEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("name"), LikePattern.contains(name), LikePattern.ESCAPE);
    }

    public static Specification<WarehouseEntity> locationContains(String location) {
        if (location == null || location.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("location"), LikePattern.contains(location), LikePattern.ESCAPE);
    }
}
