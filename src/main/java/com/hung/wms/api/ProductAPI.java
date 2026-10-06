package com.hung.wms.api;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.request.product.ProductSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.service.ProductService;
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
@RequestMapping("/api/products")
public class ProductAPI {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductResponse>> getProduct(
            @Valid ProductSearchRequest productRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<ProductResponse> result = productService.findAllProducts(productRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse result = productService.findProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Validated({OnCreate.class, Default.class}) @RequestBody ProductRequest productRequest) {
        ProductResponse result = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id,
                                         @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse result = productService.updateProduct(id, productRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}
