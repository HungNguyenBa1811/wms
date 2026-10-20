package com.hung.wms.service.impl;

import com.hung.wms.mapper.CategoryMapper;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.category.CategoryRequest;
import com.hung.wms.dto.request.category.CategorySearchRequest;
import com.hung.wms.dto.response.category.CategoryResponse;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.entity.CategoryEntity;
import com.hung.wms.repository.specification.CategorySpecification;
import com.hung.wms.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName()))
            throw new ResourceDuplicateException("Category name already exists");
        CategoryEntity category = categoryMapper.toEntity(request);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(String id, CategoryRequest request) {
        CategoryEntity category = categoryRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (request.getName() != null && categoryRepository.existsByNameAndIdNot(request.getName(), id))
            throw new ResourceDuplicateException("Category name already exists");
        modelMapper.map(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findAllCategories(CategorySearchRequest request, Pageable pageable) {
        Page<CategoryEntity> categoryEntityPage = categoryRepository.findAll(CategorySpecification.search(request), pageable);
        return new PageResponse<>(categoryEntityPage.map(categoryMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findCategoryById(String id) {
        CategoryEntity category = categoryRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategoryById(String id) {
        CategoryEntity category = categoryRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        if (productRepository.existsByCategory_IdAndIsDeletedFalse(id))
            throw new ResourceInUseException("Cannot delete category " + id + ": it still has products");
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }
}
