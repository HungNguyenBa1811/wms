package com.hung.wms.service.impl;

import com.hung.wms.converter.ProductConverter;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.response.product.ProductResponse;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.entity.CategoryEntity;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.service.ProductService;
import com.sun.jdi.request.DuplicateRequestException;
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
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductConverter productConverter;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest product) {
        ProductEntity productEntity = productConverter.toEntity(product);
        if (productRepository.existsById(product.getProductCode())) {
            throw new DuplicateRequestException("Product code already exists");
        }
        if (product.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findById(product.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategoryId()));
            productEntity.setCategory(category);
        } else {
            throw new BadRequestException("Category is empty");
        }
        return productConverter.toResponse(productRepository.save(productEntity));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest product) {
        ProductEntity productEntity = productRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (productRepository.existsByProductCodeAndIdNot(product.getProductCode(), id)) {
            throw new ResourceDuplicateException("Product code already exists");
        }
        modelMapper.map(product, productEntity);
        if (product.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findById(product.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategoryId()));
            productEntity.setCategory(category);
        } else {
            throw new BadRequestException("Category is empty");
        }
        return productConverter.toResponse(productEntity);
    }

    @Override
    public List<ProductResponse> findAllProducts(Map<String, Object> params) {
        List<ProductEntity> productEntityList = productRepository.findAllByIsDeletedFalse();
        List<ProductResponse> productResponseList = new ArrayList<>();
        for (ProductEntity items : productEntityList) {
            productResponseList.add(productConverter.toResponse(items));
        }
        return productResponseList;
    }

    @Override
    public ProductResponse findProductById(String id) {
        ProductEntity productEntity = productRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productConverter.toResponse(productEntity);
    }

    @Override
    @Transactional
    public void deleteProductById(String id) {
        ProductEntity productEntity = productRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (inventoryRepository.existsByProduct_IdAndQuantityGreaterThan(id, 0))
            throw new ResourceInUseException("Cannot delete product " + id + ": it still has stock in inventory");
        if (purchaseOrderRepository.existsByPurchaseOrderItems_Product_IdAndStatus(id, PurchaseOrderStatus.PENDING))
            throw new ResourceInUseException("Cannot delete product " + id + ": it is in a pending purchase order");
        productEntity.setIsDeleted(true);
        productRepository.save(productEntity);
    }
}
