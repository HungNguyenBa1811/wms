package com.hung.wms.repository.custom.impl;

import com.hung.wms.repository.custom.ProductCustomRepository;
import com.hung.wms.repository.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class ProductCustomRepositoryImpl implements ProductCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ProductEntity createProduct(ProductEntity product) {
        product.setId(null);
        product.setCreatedAt(LocalDateTime.now());
        entityManager.persist(product);
        return product;
    }

    @Override
    @Transactional
    public ProductEntity updateProduct(ProductEntity product) {
        ProductEntity productEntity = entityManager.find(ProductEntity.class, product.getId());
        productEntity.setProductCode(product.getProductCode());
        productEntity.setName(product.getName());
        productEntity.setDescription(product.getDescription());
        productEntity.setPrice(product.getPrice());
        productEntity.setCategory(product.getCategory());
        return productEntity;
    }
}
