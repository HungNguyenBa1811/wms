package com.hung.wms.api;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductAPI {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProduct(@RequestParam Map<String, Object> params) {
        List<ProductResponse> result = productService.findAllProducts(params);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse result = productService.findProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse result = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String id,
                                         @RequestBody ProductRequest productRequest) {
        ProductResponse result = productService.updateProduct(id, productRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build();
    }
}
