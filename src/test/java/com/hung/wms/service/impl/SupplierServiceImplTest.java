package com.hung.wms.service.impl;

import com.hung.wms.converter.SupplierConverter;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.supplier.SupplierRequest;
import com.hung.wms.model.response.supplier.SupplierResponse;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.entity.SupplierEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Supplier has no unique code, so there is no 409 duplicate case here
@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierServiceImpl")
class SupplierServiceImplTest {
    private static final String SUPPLIER_ID = "supplier-1";

    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierConverter supplierConverter;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    @Test
    @DisplayName("createSupplier: lưu và trả response")
    void createsSupplier() {
        // given
        SupplierRequest request = new SupplierRequest();
        request.setName("ACME");
        SupplierEntity entity = new SupplierEntity();
        SupplierResponse response = new SupplierResponse();
        when(supplierConverter.toEntity(request)).thenReturn(entity);
        when(supplierRepository.save(entity)).thenReturn(entity);
        when(supplierConverter.toResponse(entity)).thenReturn(response);

        // when
        SupplierResponse result = supplierService.createSupplier(request);

        // then
        assertThat(result).isSameAs(response);
    }

    @Test
    @DisplayName("updateSupplier: không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
    void updateNotFound() {
        // given
        when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> supplierService.updateSupplier(SUPPLIER_ID, new SupplierRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("findSupplierById: không tồn tại -> ResourceNotFoundException (404)")
    void findByIdNotFound() {
        // given
        when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> supplierService.findSupplierById(SUPPLIER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(SUPPLIER_ID);
    }

    @Nested
    @DisplayName("deleteSupplierById")
    class Delete {
        @Test
        @DisplayName("không tồn tại hoặc đã xóa -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> supplierService.deleteSupplierById(SUPPLIER_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("có PO PENDING -> ResourceInUseException (409), không xóa")
        void hasPendingPurchaseOrder() {
            // given
            SupplierEntity entity = supplier();
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.of(entity));
            when(purchaseOrderRepository.existsBySupplier_IdAndStatus(SUPPLIER_ID, PurchaseOrderStatus.PENDING)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> supplierService.deleteSupplierById(SUPPLIER_ID))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("pending purchase order");
            assertThat(entity.getIsDeleted()).isFalse();
            verify(supplierRepository, never()).save(any());
        }

        @Test
        @DisplayName("không có PO PENDING -> xóa mềm (isDeleted = true)")
        void softDeletes() {
            // given
            SupplierEntity entity = supplier();
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.of(entity));
            when(purchaseOrderRepository.existsBySupplier_IdAndStatus(SUPPLIER_ID, PurchaseOrderStatus.PENDING)).thenReturn(false);

            // when
            supplierService.deleteSupplierById(SUPPLIER_ID);

            // then
            assertThat(entity.getIsDeleted()).isTrue();
            verify(supplierRepository).save(entity);
            verify(supplierRepository, never()).deleteById(any());
        }
    }

    private static SupplierEntity supplier() {
        SupplierEntity supplier = new SupplierEntity();
        supplier.setId(SUPPLIER_ID);
        supplier.setName("ACME");
        return supplier;
    }
}
