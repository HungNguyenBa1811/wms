package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.purchaseorder.PurchaseOrderSearchRequest;
import com.hung.wms.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.equal;

public class PurchaseOrderSpecification {
    private PurchaseOrderSpecification() {
    }

    public static Specification<PurchaseOrderEntity> search(PurchaseOrderSearchRequest request) {
        return Specification.allOf(
                equal("status", request.getStatus()),
                equal("supplier.id", request.getSupplierId()),
                equal("warehouse.id", request.getWarehouseId())
        );
    }
}
