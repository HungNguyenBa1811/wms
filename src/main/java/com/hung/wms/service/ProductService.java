package com.hung.wms.service;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.request.product.ProductSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.product.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(ProductRequest product);
    ProductResponse updateProduct(String id, ProductRequest product);
    PageResponse<ProductResponse> findAllProducts(ProductSearchRequest request, Pageable pageable);
    ProductResponse findProductById(String id);
    void deleteProductById(String id);
}
