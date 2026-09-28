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

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<ProductResponse> findAllProducts() {
        List<ProductEntity> productEntityList = productRepository.findAll();
        List<ProductResponse> productResponseList = new ArrayList<>();
        for (ProductEntity items : productEntityList) {
            productResponseList.add(productConverter.toResponse(items));
        }
        return productResponseList;
    }

    @Override
    public ProductResponse findProductById(String id) {
        ProductEntity productEntity = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        return productConverter.toResponse(productEntity);
    }

    @Override
    @Transactional
    public void deleteProductById(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
