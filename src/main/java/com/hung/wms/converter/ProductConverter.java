package com.hung.wms.converter;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.entity.CategoryEntity;
import com.hung.wms.repository.entity.ProductEntity;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    public ProductEntity toEntity(ProductRequest request) {
        ProductEntity product = modelMapper.map(request, ProductEntity.class);
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }
        return product;
    }

    public ProductResponse toResponse(ProductEntity product) {
        return modelMapper.map(product, ProductResponse.class);
    }
}
