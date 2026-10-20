package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.product.ProductSearchRequest;
import com.hung.wms.entity.ProductEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.contains;
import static com.hung.wms.repository.specification.SpecificationUtils.equal;
import static com.hung.wms.repository.specification.SpecificationUtils.lessThan;
import static com.hung.wms.repository.specification.SpecificationUtils.notDeleted;
import static com.hung.wms.repository.specification.SpecificationUtils.range;
import static com.hung.wms.repository.specification.SpecificationUtils.startsWith;

// Codes use startsWith (prefix LIKE) so MySQL can use the unique index of the code column; other text uses contains
public class ProductSpecification {
    private ProductSpecification() {
    }

    public static Specification<ProductEntity> search(ProductSearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                contains("name", request.getName()),
                startsWith("productCode", request.getProductCode()),
                equal("category.id", request.getCategoryId()),
                range("price", request.getPriceFrom(), request.getPriceTo()),
                lessThan("expiryDate", request.getExpiryBefore())
        );
    }
}
