package com.hung.wms.repository.custom;

import com.hung.wms.repository.entity.ProductEntity;

public interface ProductCustomRepository {
    ProductEntity createProduct(ProductEntity product);
    ProductEntity updateProduct(ProductEntity product);
}
