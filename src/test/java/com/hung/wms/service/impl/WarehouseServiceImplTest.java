package com.hung.wms.service.impl;

import com.hung.wms.converter.WarehouseConverter;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.response.warehouse.WarehouseResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.repository.entity.WarehouseEntity;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("WarehouseServiceImpl")
class WarehouseServiceImplTest {
    private static final String WAREHOUSE_ID = "warehouse-1";

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private WarehouseConverter warehouseConverter;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    @Nested
    @DisplayName("createWarehouse")
    class Create {
        @Test
        @DisplayName("trùng warehouseCode -> ResourceDuplicateException (409), không lưu")
        void duplicateCode() {
            // given
            when(warehouseRepository.existsByWarehouseCode("WH-001")).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> warehouseService.createWarehouse(request("WH-001")))
                    .isInstanceOf(ResourceDuplicateException.class);
            verify(warehouseRepository, never()).save(any());
        }

        @Test
        @DisplayName("thành công -> lưu và trả response")
        void createsWarehouse() {
            // given
            WarehouseRequest request = request("WH-009");
            WarehouseEntity entity = new WarehouseEntity();
            WarehouseResponse response = new WarehouseResponse();
            when(warehouseRepository.existsByWarehouseCode("WH-009")).thenReturn(false);
            when(warehouseConverter.toEntity(request)).thenReturn(entity);
            when(warehouseRepository.save(entity)).thenReturn(entity);
            when(warehouseConverter.toResponse(entity)).thenReturn(response);

            // when
            WarehouseResponse result = warehouseService.createWarehouse(request);

            // then
            assertThat(result).isSameAs(response);
        }
    }

    @Nested
    @DisplayName("updateWarehouse")
    class Update {
        @Test
        @DisplayName("đổi sang warehouseCode của kho khác -> ResourceDuplicateException (409)")
        void duplicateCode() {
            // given
            when(warehouseRepository.existsByWarehouseCodeAndIdNot("WH-002", WAREHOUSE_ID)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> warehouseService.updateWarehouse(WAREHOUSE_ID, request("WH-002")))
                    .isInstanceOf(ResourceDuplicateException.class);
            verify(warehouseRepository, never()).save(any());
        }

        @Test
        @DisplayName("không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(warehouseRepository.existsByWarehouseCodeAndIdNot("WH-001", WAREHOUSE_ID)).thenReturn(false);
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> warehouseService.updateWarehouse(WAREHOUSE_ID, request("WH-001")))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("thành công -> map request vào chính entity đang có rồi lưu")
        void mapsIntoExistingEntity() {
            // given
            WarehouseRequest request = request("WH-001");
            WarehouseEntity entity = warehouse();
            when(warehouseRepository.existsByWarehouseCodeAndIdNot("WH-001", WAREHOUSE_ID)).thenReturn(false);
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(entity));
            when(warehouseRepository.save(entity)).thenReturn(entity);

            // when
            warehouseService.updateWarehouse(WAREHOUSE_ID, request);

            // then
            verify(modelMapper).map(request, entity);
            verify(warehouseRepository).save(entity);
        }
    }

    @Test
    @DisplayName("findWarehouseById: không tồn tại -> ResourceNotFoundException (404)")
    void findByIdNotFound() {
        // given
        when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> warehouseService.findWarehouseById(WAREHOUSE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Nested
    @DisplayName("deleteWarehouseById")
    class Delete {
        @Test
        @DisplayName("không tồn tại hoặc đã xóa -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> warehouseService.deleteWarehouseById(WAREHOUSE_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("còn tồn kho > 0 -> ResourceInUseException (409), không xóa")
        void stillHasStock() {
            // given
            WarehouseEntity entity = warehouse();
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(entity));
            when(inventoryRepository.existsByWarehouse_IdAndQuantityGreaterThan(WAREHOUSE_ID, 0)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> warehouseService.deleteWarehouseById(WAREHOUSE_ID))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("stock");
            assertThat(entity.getIsDeleted()).isFalse();
            verify(warehouseRepository, never()).save(any());
        }

        @Test
        @DisplayName("có PO PENDING -> ResourceInUseException (409), không xóa")
        void hasPendingPurchaseOrder() {
            // given
            WarehouseEntity entity = warehouse();
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(entity));
            when(inventoryRepository.existsByWarehouse_IdAndQuantityGreaterThan(WAREHOUSE_ID, 0)).thenReturn(false);
            when(purchaseOrderRepository.existsByWarehouse_IdAndStatus(WAREHOUSE_ID, PurchaseOrderStatus.PENDING)).thenReturn(true);

            // when / then
            assertThatThrownBy(() -> warehouseService.deleteWarehouseById(WAREHOUSE_ID))
                    .isInstanceOf(ResourceInUseException.class)
                    .hasMessageContaining("pending purchase order");
            assertThat(entity.getIsDeleted()).isFalse();
            verify(warehouseRepository, never()).save(any());
        }

        @Test
        @DisplayName("không còn được dùng -> xóa mềm (isDeleted = true)")
        void softDeletes() {
            // given
            WarehouseEntity entity = warehouse();
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(entity));
            when(inventoryRepository.existsByWarehouse_IdAndQuantityGreaterThan(WAREHOUSE_ID, 0)).thenReturn(false);
            when(purchaseOrderRepository.existsByWarehouse_IdAndStatus(WAREHOUSE_ID, PurchaseOrderStatus.PENDING)).thenReturn(false);

            // when
            warehouseService.deleteWarehouseById(WAREHOUSE_ID);

            // then
            assertThat(entity.getIsDeleted()).isTrue();
            verify(warehouseRepository).save(entity);
            verify(warehouseRepository, never()).deleteById(any());
        }
    }

    private static WarehouseRequest request(String code) {
        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseCode(code);
        request.setName("Main");
        request.setLocation("HCMC");
        return request;
    }

    private static WarehouseEntity warehouse() {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(WAREHOUSE_ID);
        warehouse.setWarehouseCode("WH-001");
        return warehouse;
    }
}
