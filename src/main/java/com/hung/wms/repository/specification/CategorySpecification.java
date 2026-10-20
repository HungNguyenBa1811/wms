package com.hung.wms.repository.specification;

import com.hung.wms.model.request.category.CategorySearchRequest;
import com.hung.wms.repository.entity.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {
    private CategorySpecification() {
    }

    public static Specification<CategoryEntity> search(CategorySearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                nameContains(request.getName()),
                descriptionContains(request.getDescription())
        );
    }

    public static Specification<CategoryEntity> notDeleted() {
        return (root, query, cb) -> cb.isFalse(root.get("isDeleted"));
    }

    public static Specification<CategoryEntity> nameContains(String name) {
        if (name == null || name.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("name"), LikePattern.contains(name), LikePattern.ESCAPE);
    }

    public static Specification<CategoryEntity> descriptionContains(String description) {
        if (description == null || description.isBlank()) return Specification.unrestricted();
        return (root, query, cb) -> cb.like(root.get("description"), LikePattern.contains(description), LikePattern.ESCAPE);
    }
}
