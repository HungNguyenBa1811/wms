package com.hung.wms.api;

import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// TODO: Implement error handling && params
@RestController
@RequestMapping("/api/products")
public class ProductAPI {
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductResponse> getProduct(@RequestParam Map<String, Object> params) {
        List<ProductResponse> result = productService.findAllProducts();
        return result;
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable String id) {
        ProductResponse result = productService.findProductById(id);
        return result;
    }

    @PostMapping
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse result = productService.createProduct(productRequest);
        return result;
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable String id,
                                         @RequestBody ProductRequest productRequest) {
        ProductResponse result = productService.updateProduct(id, productRequest);
        return result;
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProductById(id);
    }
}
