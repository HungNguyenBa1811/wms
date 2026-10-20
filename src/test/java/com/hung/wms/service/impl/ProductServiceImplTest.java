package com.hung.wms.service.impl;

import com.hung.wms.mapper.ProductMapper;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.product.ProductRequest;
import com.hung.wms.dto.response.product.ProductResponse;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.entity.CategoryEntity;
import com.hung.wms.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {
    private static final String PRODUCT_ID = "product-1";
    private static final String CATEGORY_ID = "category-1";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("createProduct")
    class Create {
        @Test
        @DisplayName("trùng productCode -> ResourceDuplicateException (409), không lưu")
        void duplicateCode() {
            // given
            ProductRequest request = request("P-001", CATEGORY_ID);
            when(productMapper.toEntity(request)).thenReturn(new ProductEntity());
            when(productRepository.existsByProductCode("P-001")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ResourceDuplicateException.class);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("category không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void categoryNotFound() {
            // given
            ProductRequest request = request("P-001", CATEGORY_ID);
            when(productMapper.toEntity(request)).thenReturn(new ProductEntity());
            when(productRepository.existsByProductCode("P-001")).thenReturn(false);
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(CATEGORY_ID);
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("thành công: gán category đã resolve rồi lưu")
        void createsProduct() {
            // given
            ProductRequest request = request("P-001", CATEGORY_ID);
            ProductEntity entity = new ProductEntity();
            CategoryEntity category = category();
            ProductResponse response = new ProductResponse();
            when(productMapper.toEntity(request)).thenReturn(entity);
            when(productRepository.existsByProductCode("P-001")).thenReturn(false);
            when(categoryRepository.findByIdAndIsDeletedFalse(CATEGORY_ID)).thenReturn(Optional.of(category));
            when(productRepository.save(entity)).thenReturn(entity);
            when(productMapper.toResponse(entity)).thenReturn(response);

            // when
            ProductResponse result = productService.createProduct(request);

            // then
            assertThat(entity.getCategory()).isSameAs(category);
            assertThat(result).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("updateProduct")
    class Update {
        @Test
        @DisplayName("không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request("P-001", null)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("đổi sang productCode của product khác -> ResourceDuplicateException (409)")
        void duplicateCode() {
            // given
            ProductEntity entity = product();
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.of(entity));
            when(productRepository.existsByProductCodeAndIdNot("P-002", PRODUCT_ID)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, request("P-002", null)))
                    .isInstanceOf(ResourceDuplicateException.class);
            verify(modelMapper, never()).map(any(), any());
        }

        @Test
        @DisplayName("không gửi categoryId -> giữ category cũ")
        void keepsCategoryWhenNotSent() {
            // given
            ProductEntity entity = product();
            CategoryEntity oldCategory = entity.getCategory();
            ProductRequest request = request(null, null);
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.of(entity));
            when(productRepository.existsByProductCodeAndIdNot(null, PRODUCT_ID)).thenReturn(false);

            // when
            productService.updateProduct(PRODUCT_ID, request);

            // then
            verify(modelMapper).map(request, entity);
            assertThat(entity.getCategory()).isSameAs(oldCategory);
        }
    }

    @Test
    @DisplayName("findProductById: không tồn tại -> ResourceNotFoundException (404)")
    void findByIdNotFound() {
        // given
        when(productRepository.findByIdAndIsDeletedFalse(PRODUCT_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> productService.findProductById(PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(PRODUCT_ID);
    }

    @Nested
    @DisplayName("deleteProductById")
    class Delete {
        @Test
        @DisplayName("không tồn tại hoặc đã xóa -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> productService.deleteProductById(PRODUCT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("còn tồn kho > 0 -> ResourceInUseException (409), không xóa")
        void stillHasStock() {
            // given
            ProductEntity entity = product();
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.of(entity));
            when(inventoryRepository.existsByProduct_IdAndQuantityGreaterThan(PRODUCT_ID, 0)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> productService.deleteProductById(PRODUCT_ID))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("stock");
            assertThat(entity.getIsDeleted()).isFalse();
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("nằm trong PO PENDING -> ResourceInUseException (409), không xóa")
        void inPendingPurchaseOrder() {
            // given
            ProductEntity entity = product();
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.of(entity));
            when(inventoryRepository.existsByProduct_IdAndQuantityGreaterThan(PRODUCT_ID, 0)).thenReturn(false);
            when(purchaseOrderRepository.existsByPurchaseOrderItems_Product_IdAndStatus(PRODUCT_ID, PurchaseOrderStatus.PENDING))
                    .thenReturn(true);

            // when / then
            assertThatThrownBy(() -> productService.deleteProductById(PRODUCT_ID))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("pending purchase order");
            assertThat(entity.getIsDeleted()).isFalse();
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("không còn được dùng -> xóa mềm (isDeleted = true)")
        void softDeletes() {
            // given
            ProductEntity entity = product();
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_ID)).thenReturn(Optional.of(entity));
            when(inventoryRepository.existsByProduct_IdAndQuantityGreaterThan(PRODUCT_ID, 0)).thenReturn(false);
            when(purchaseOrderRepository.existsByPurchaseOrderItems_Product_IdAndStatus(PRODUCT_ID, PurchaseOrderStatus.PENDING))
                    .thenReturn(false);

            // when
            productService.deleteProductById(PRODUCT_ID);

            // then
            assertThat(entity.getIsDeleted()).isTrue();
            verify(productRepository).save(entity);
            verify(productRepository, never()).delete(any(ProductEntity.class));
            verify(productRepository, never()).deleteById(any());
        }
    }

    private static ProductRequest request(String productCode, String categoryId) {
        ProductRequest request = new ProductRequest();
        request.setProductCode(productCode);
        request.setName("Mouse");
        request.setPrice(new BigDecimal("10.00"));
        request.setCategoryId(categoryId);
        return request;
    }

    private static CategoryEntity category() {
        CategoryEntity category = new CategoryEntity();
        category.setId(CATEGORY_ID);
        category.setName("Electronics");
        return category;
    }

    private static ProductEntity product() {
        ProductEntity product = new ProductEntity();
        product.setId(PRODUCT_ID);
        product.setProductCode("P-001");
        product.setCategory(category());
        return product;
    }
}
