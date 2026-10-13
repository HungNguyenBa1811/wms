package com.hung.wms.service;

import com.hung.wms.model.request.category.CategoryRequest;
import com.hung.wms.model.request.category.CategorySearchRequest;
import com.hung.wms.model.response.category.CategoryResponse;
import com.hung.wms.model.response.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(String id, CategoryRequest request);
    PageResponse<CategoryResponse> findAllCategories(CategorySearchRequest request, Pageable pageable);
    CategoryResponse findCategoryById(String id);
    void deleteCategoryById(String id);
}
