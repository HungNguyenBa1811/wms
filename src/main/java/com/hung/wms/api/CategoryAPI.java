package com.hung.wms.api;

import com.hung.wms.model.request.category.CategoryRequest;
import com.hung.wms.model.request.category.CategorySearchRequest;
import com.hung.wms.model.response.category.CategoryResponse;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.service.CategoryService;
import com.hung.wms.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryAPI {
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<PageResponse<CategoryResponse>> getCategory(
            @Valid CategorySearchRequest categorySearchRequest,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<CategoryResponse> result = categoryService.findAllCategories(categorySearchRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable String id) {
        CategoryResponse result = categoryService.findCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Validated({OnCreate.class, Default.class}) @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse result = categoryService.createCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable String id,
                                                           @Valid @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse result = categoryService.updateCategory(id, categoryRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategoryById(id);
        return ResponseEntity.noContent().build();
    }
}
