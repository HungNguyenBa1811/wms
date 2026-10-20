package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.product.ProductSearchRequest;
import com.hung.wms.entity.ProductEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {
    private ProductSpecification() {
    }

    public static Specification<ProductEntity> search(ProductSearchRequest productSearchRequest) {
        return Specification.allOf(
                notDeleted(),
                nameContains(productSearchRequest.getName()),
                productCodeStartsWith(productSearchRequest.getProductCode()),
                hasCategory(productSearchRequest.getCategoryId()),
                priceFrom(productSearchRequest.getPriceFrom()),
                priceTo(productSearchRequest.getPriceTo())
        );
    }

    public static Specification<ProductEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<ProductEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("name"), LikePattern.contains(name), LikePattern.ESCAPE);
    }

    // Prefix search: "SEED" matches "SEED-001" but "001" does not. Uses the unique index on the code column
    public static Specification<ProductEntity> productCodeStartsWith(String productCode) {
        if (productCode == null || productCode.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("productCode"), LikePattern.startsWith(productCode), LikePattern.ESCAPE);
    }

    public static Specification<ProductEntity> hasCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<ProductEntity> priceFrom(BigDecimal priceFrom) {
        if (priceFrom == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), priceFrom);
    }

    public static Specification<ProductEntity> priceTo(BigDecimal priceTo) {
        if (priceTo == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), priceTo);
    }
}
