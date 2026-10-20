package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.inventory.InventorySearchRequest;
import com.hung.wms.entity.InventoryEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.equal;

public class InventorySpecification {
    private InventorySpecification() {
    }

    public static Specification<InventoryEntity> search(InventorySearchRequest request) {
        return Specification.allOf(
                equal("warehouse.id", request.getWarehouseId()),
                equal("product.id", request.getProductId())
        );
    }
}
