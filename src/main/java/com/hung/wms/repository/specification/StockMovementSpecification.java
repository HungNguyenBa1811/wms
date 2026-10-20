package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.stockmovement.StockMovementSearchRequest;
import com.hung.wms.entity.StockMovementEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.equal;

public class StockMovementSpecification {
    private StockMovementSpecification() {
    }

    public static Specification<StockMovementEntity> search(StockMovementSearchRequest request) {
        return Specification.allOf(
                equal("warehouse.id", request.getWarehouseId()),
                equal("product.id", request.getProductId()),
                equal("movementType", request.getMovementType())
        );
    }
}
