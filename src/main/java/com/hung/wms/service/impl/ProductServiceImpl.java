package com.hung.wms.service.impl;

import com.hung.wms.converter.ProductConverter;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.entity.CategoryEntity;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductConverter productConverter;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest product) {
        ProductEntity productEntity = productConverter.toEntity(product);
        if (product.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findById(product.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategoryId()));
            productEntity.setCategory(category);
        }
        return productConverter.toResponse(productRepository.save(productEntity));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest product) {
        ProductEntity productEntity = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        modelMapper.map(product, productEntity);
        if (product.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findById(product.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategoryId()));
            productEntity.setCategory(category);
        }
        return productConverter.toResponse(productEntity);
    }

    @Override
    public List<ProductResponse> findAllProducts(Map<String, Object> params) {
        List<ProductEntity> productEntityList = productRepository.findAll();
        List<ProductResponse> productResponseList = new ArrayList<>();
        for (ProductEntity items : productEntityList) {
            productResponseList.add(productConverter.toResponse(items));
        }
        return productResponseList;
    }

    @Override
    public ProductResponse findProductById(String id) {
        ProductEntity productEntity = productRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productConverter.toResponse(productEntity);
    }

    @Override
    @Transactional
    public void deleteProductById(String id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
