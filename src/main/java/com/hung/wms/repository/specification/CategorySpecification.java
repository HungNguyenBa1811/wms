package com.hung.wms.repository.specification;

import com.hung.wms.dto.request.category.CategorySearchRequest;
import com.hung.wms.entity.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;

import static com.hung.wms.repository.specification.SpecificationUtils.contains;
import static com.hung.wms.repository.specification.SpecificationUtils.notDeleted;

public class CategorySpecification {
    private CategorySpecification() {
    }

    public static Specification<CategoryEntity> search(CategorySearchRequest request) {
        return Specification.allOf(
                notDeleted(),
                contains("name", request.getName()),
                contains("description", request.getDescription())
        );
    }
}
