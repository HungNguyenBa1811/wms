package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.supplier.SupplierSearchRequest;
import com.hung.wms.entity.SupplierEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.contains;
import static com.hung.wms.repository.specification.SpecificationUtils.notDeleted;

public class SupplierSpecification {
    private SupplierSpecification() {
    }

    public static Specification<SupplierEntity> search(SupplierSearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                contains("name", request.getName()),
                contains("phone", request.getPhone()),
                contains("email", request.getEmail()),
                contains("address", request.getAddress())
        );
    }
}
