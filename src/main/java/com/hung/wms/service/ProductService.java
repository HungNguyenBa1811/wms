package com.hung.wms.service;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductResponse createProduct(ProductRequest product);
    ProductResponse updateProduct(String id, ProductRequest product);
    List<ProductResponse> findAllProducts(Map<String, Object> params);
    ProductResponse findProductById(String id);
    void deleteProductById(String id);
}
