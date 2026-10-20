package com.hung.wms.service.impl;

import com.hung.wms.mapper.ProductMapper;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.product.ProductRequest;
import com.hung.wms.dto.request.product.ProductSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.product.ProductResponse;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.entity.CategoryEntity;
import com.hung.wms.entity.ProductEntity;
import com.hung.wms.repository.specification.ProductSpecification;
import com.hung.wms.service.ProductService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    private ProductMapper productMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest product) {
        ProductEntity productEntity = productMapper.toEntity(product);
        if (productRepository.existsByProductCode(product.getProductCode())) {
            throw new ResourceDuplicateException("Product code already exists");
        }
        if (product.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findByIdAndIsDeletedFalse(product.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategoryId()));
            productEntity.setCategory(category);
        }
        if (productEntity.getImportedAt() == null)
            productEntity.setImportedAt(LocalDate.now());
        checkExpiryDate(productEntity);
        return productMapper.toResponse(productRepository.save(productEntity));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String id, ProductRequest product) {
        ProductEntity productEntity = productRepository
                .findByIdAndIsDeletedFalseForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (productRepository.existsByProductCodeAndIdNot(product.getProductCode(), id)) {
            throw new ResourceDuplicateException("Product code already exists");
        }
        modelMapper.map(product, productEntity);
        if (product.getCategoryId() != null) {
            CategoryEntity category = categoryRepository
                    .findByIdAndIsDeletedFalse(product.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + product.getCategoryId()));
            productEntity.setCategory(category);
        }
        checkExpiryDate(productEntity);
        return productMapper.toResponse(productEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllProducts(ProductSearchRequest productSearchRequest, Pageable pageable) {
        Page<ProductEntity> productEntityPage = productRepository.findAll(ProductSpecification.search(productSearchRequest), pageable);
        return new PageResponse<>(productEntityPage.map(productMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findProductById(String id) {
        ProductEntity productEntity = productRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toResponse(productEntity);
    }

    @Override
    @Transactional
    public void deleteProductById(String id) {
        ProductEntity productEntity = productRepository
                .findByIdAndIsDeletedFalseForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (inventoryRepository.existsByProduct_IdAndQuantityGreaterThan(id, 0))
            throw new ResourceInUseException("Cannot delete product " + id + ": it still has stock in inventory");
        if (purchaseOrderRepository.existsByPurchaseOrderItems_Product_IdAndStatus(id, PurchaseOrderStatus.PENDING))
            throw new ResourceInUseException("Cannot delete product " + id + ": it is in a pending purchase order");
        productEntity.setIsDeleted(true);
        productRepository.save(productEntity);
    }

    // The request only validates the pair when both dates are sent. Here the dates are the final values after
    // merging the request into the entity (create: importedAt defaults to today; PUT: one date may come from the DB)
    private void checkExpiryDate(ProductEntity productEntity) {
        if (productEntity.getExpiryDate() != null && productEntity.getExpiryDate().isBefore(productEntity.getImportedAt()))
            throw new BadRequestException("Expiry date must not be before imported date");
    }
}
