package com.hung.wms.service.impl;

import com.hung.wms.converter.ProductConverter;
import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductConverter productConverter;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest product) {
        ProductEntity productEntity = productConverter.toEntity(product);
        return productConverter.toResponse(productRepository.createProduct(productEntity));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest product) {
        ProductEntity productEntity = productConverter.toEntity(product);
        productEntity.setId(id);
        return productConverter.toResponse(productRepository.updateProduct(productEntity));
    }
}
