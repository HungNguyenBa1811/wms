package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.warehouse.WarehouseSearchRequest;
import com.hung.wms.entity.WarehouseEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.contains;
import static com.hung.wms.repository.specification.SpecificationUtils.notDeleted;
import static com.hung.wms.repository.specification.SpecificationUtils.startsWith;

// Codes use startsWith (prefix LIKE) so MySQL can use the unique index of the code column; other text uses contains
public class WarehouseSpecification {
    private WarehouseSpecification() {
    }

    public static Specification<WarehouseEntity> search(WarehouseSearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                startsWith("warehouseCode", request.getWarehouseCode()),
                contains("name", request.getName()),
                contains("location", request.getLocation())
        );
    }
}
