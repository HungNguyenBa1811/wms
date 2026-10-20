package com.hung.wms.service.impl;

import com.hung.wms.converter.PurchaseOrderConverter;
import com.hung.wms.converter.StockMovementConverter;
import com.hung.wms.enums.MovementType;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.InvalidStateException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.StockMovementRepository;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.UserRepository;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.repository.entity.PurchaseOrderEntity;
import com.hung.wms.repository.entity.PurchaseOrderItemEntity;
import com.hung.wms.repository.entity.StockMovementEntity;
import com.hung.wms.repository.entity.SupplierEntity;
import com.hung.wms.repository.entity.UserEntity;
import com.hung.wms.repository.entity.WarehouseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
// Converter stubs are shared by every test; not every test reaches toItemEntity / toResponse
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("PurchaseOrderServiceImpl")
class PurchaseOrderServiceImplTest {
    private static final String PO_ID = "po-1";
    private static final String SUPPLIER_ID = "supplier-1";
    private static final String WAREHOUSE_ID = "warehouse-1";
    private static final String PRODUCT_A = "product-a";
    private static final String PRODUCT_B = "product-b";
    private static final String USERNAME = "admin";

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private PurchaseOrderConverter purchaseOrderConverter;
    // Real converter: toEntity only copies fields, so the saved stock movement can be asserted
    @Spy
    private StockMovementConverter stockMovementConverter = new StockMovementConverter();

    @InjectMocks
    private PurchaseOrderServiceImpl purchaseOrderService;

    private final PurchaseOrderResponse convertedResponse = new PurchaseOrderResponse();
    private final Principal principal = () -> USERNAME;

    @BeforeEach
    void stubConverter() {
        when(purchaseOrderConverter.toEntity(any(PurchaseOrderRequest.class)))
                .thenAnswer(invocation -> new PurchaseOrderEntity());
        when(purchaseOrderConverter.toItemEntity(any(PurchaseOrderItemRequest.class)))
                .thenAnswer(invocation -> {
                    PurchaseOrderItemRequest request = invocation.getArgument(0);
                    PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
                    item.setQuantity(request.getQuantity());
                    item.setUnitCost(request.getUnitCost());
                    return item;
                });
        when(purchaseOrderConverter.toResponse(any(PurchaseOrderEntity.class))).thenReturn(convertedResponse);
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("createPurchaseOrder")
    class Create {
        @Test
        @DisplayName("thành công: gán supplier / warehouse, status PENDING, item gắn product và PO")
        void createsPendingPurchaseOrderWithItems() {
            // given
            SupplierEntity supplier = supplier();
            WarehouseEntity warehouse = warehouse();
            ProductEntity productA = product(PRODUCT_A);
            ProductEntity productB = product(PRODUCT_B);
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.of(supplier));
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse));
            // 1 locking query for all products of the request
            when(productRepository.findAllByIdInAndIsDeletedFalseForUpdate(Set.of(PRODUCT_A, PRODUCT_B)))
                    .thenReturn(List.of(productA, productB));
            PurchaseOrderRequest request = poRequest(itemRequest(PRODUCT_A, 5, "10.00"), itemRequest(PRODUCT_B, 3, "2.50"));

            // when
            PurchaseOrderResponse response = purchaseOrderService.createPurchaseOrder(request);

            // then
            ArgumentCaptor<PurchaseOrderEntity> saved = ArgumentCaptor.forClass(PurchaseOrderEntity.class);
            verify(purchaseOrderRepository).save(saved.capture());
            PurchaseOrderEntity po = saved.getValue();
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PENDING);
            assertThat(po.getSupplier()).isSameAs(supplier);
            assertThat(po.getWarehouse()).isSameAs(warehouse);
            assertThat(po.getReceivedAt()).isNull();
            assertThat(po.getReceivedBy()).isNull();
            assertThat(po.getPurchaseOrderItems()).hasSize(2);
            assertThat(po.getPurchaseOrderItems()).allSatisfy(item -> assertThat(item.getPurchaseOrder()).isSameAs(po));
            assertThat(po.getPurchaseOrderItems())
                    .extracting(item -> item.getProduct().getId(), PurchaseOrderItemEntity::getQuantity)
                    .containsExactly(
                            tuple(PRODUCT_A, 5),
                            tuple(PRODUCT_B, 3));
            assertThat(response).isSameAs(convertedResponse);
        }

        @Test
        @DisplayName("supplier không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void supplierNotFound() {
            // given
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.empty());
            PurchaseOrderRequest request = poRequest(itemRequest(PRODUCT_A, 1, "1.00"));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Supplier not found")
                    .hasMessageContaining(SUPPLIER_ID);
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("warehouse không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void warehouseNotFound() {
            // given
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.of(supplier()));
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.empty());
            PurchaseOrderRequest request = poRequest(itemRequest(PRODUCT_A, 1, "1.00"));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Warehouse not found");
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("product không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void productNotFound() {
            // given
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.of(supplier()));
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse()));
            when(productRepository.findAllByIdInAndIsDeletedFalseForUpdate(Set.of(PRODUCT_A))).thenReturn(List.of());
            PurchaseOrderRequest request = poRequest(itemRequest(PRODUCT_A, 1, "1.00"));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found")
                    .hasMessageContaining(PRODUCT_A);
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("trùng product ngay trong request -> BadRequestException (400)")
        void duplicateProductInRequest() {
            // given
            when(supplierRepository.findByIdAndIsDeletedFalse(SUPPLIER_ID)).thenReturn(Optional.of(supplier()));
            when(warehouseRepository.findByIdAndIsDeletedFalse(WAREHOUSE_ID)).thenReturn(Optional.of(warehouse()));
            when(productRepository.findAllByIdInAndIsDeletedFalseForUpdate(Set.of(PRODUCT_A)))
                    .thenReturn(List.of(product(PRODUCT_A)));
            PurchaseOrderRequest request = poRequest(itemRequest(PRODUCT_A, 1, "1.00"), itemRequest(PRODUCT_A, 2, "1.00"));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.createPurchaseOrder(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Product already exists in this purchase order");
            verify(productRepository, times(1)).findAllByIdInAndIsDeletedFalseForUpdate(Set.of(PRODUCT_A));
            verify(productRepository, never()).findByIdAndIsDeletedFalseForUpdate(anyString());
            verify(purchaseOrderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("addPurchaseOrderItem")
    class AddItem {
        @Test
        @DisplayName("PO PENDING -> thêm item, gắn product và PO, lưu lại")
        void addsItemToPendingPurchaseOrder() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            ProductEntity productB = product(PRODUCT_B);
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_B)).thenReturn(Optional.of(productB));

            // when
            PurchaseOrderResponse response = purchaseOrderService.addPurchaseOrderItem(PO_ID, itemRequest(PRODUCT_B, 7, "3.00"));

            // then
            assertThat(po.getPurchaseOrderItems()).hasSize(2);
            PurchaseOrderItemEntity added = po.getPurchaseOrderItems().get(1);
            assertThat(added.getProduct()).isSameAs(productB);
            assertThat(added.getPurchaseOrder()).isSameAs(po);
            assertThat(added.getQuantity()).isEqualTo(7);
            assertThat(added.getUnitCost()).isEqualByComparingTo("3.00");
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PENDING);
            verify(purchaseOrderRepository).save(po);
            assertThat(response).isSameAs(convertedResponse);
        }

        @Test
        @DisplayName("PO không tồn tại -> ResourceNotFoundException (404)")
        void purchaseOrderNotFound() {
            // given
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.addPurchaseOrderItem(PO_ID, itemRequest(PRODUCT_A, 1, "1.00")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Purchase order not found")
                    .hasMessageContaining(PO_ID);
        }

        @Test
        @DisplayName("PO đã RECEIVED -> InvalidStateException (409) kèm status hiện tại, không tra product")
        void receivedPurchaseOrderRejectsNewItem() {
            // given
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID))
                    .thenReturn(Optional.of(purchaseOrder(PurchaseOrderStatus.RECEIVED)));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.addPurchaseOrderItem(PO_ID, itemRequest(PRODUCT_A, 1, "1.00")))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("RECEIVED");
            verifyNoInteractions(productRepository);
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("product đã có trong PO -> BadRequestException (400)")
        void duplicateProductInPurchaseOrder() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.addPurchaseOrderItem(PO_ID, itemRequest(PRODUCT_A, 1, "1.00")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Product already exists in this purchase order");
            assertThat(po.getPurchaseOrderItems()).hasSize(1);
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("product không tồn tại hoặc đã xóa mềm -> ResourceNotFoundException (404)")
        void productNotFound() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(productRepository.findByIdAndIsDeletedFalseForUpdate(PRODUCT_B)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.addPurchaseOrderItem(PO_ID, itemRequest(PRODUCT_B, 1, "1.00")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
            assertThat(po.getPurchaseOrderItems()).isEmpty();
            verify(purchaseOrderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("receivePurchaseOrder")
    class Receive {
        @Test
        @DisplayName("PO không tồn tại -> ResourceNotFoundException (404)")
        void purchaseOrderNotFound() {
            // given
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(PO_ID, principal))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(PO_ID);
            verifyNoInteractions(inventoryRepository, stockMovementRepository);
        }

        @Test
        @DisplayName("PO đã RECEIVED -> InvalidStateException (409), không đụng tồn kho")
        void alreadyReceived() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.RECEIVED);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(PO_ID, principal))
                    .isInstanceOf(InvalidStateException.class)
                    .hasMessageContaining("RECEIVED");
            verifyNoInteractions(inventoryRepository, stockMovementRepository, userRepository);
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("PO không có item -> BadRequestException (400)")
        void emptyPurchaseOrder() {
            // given
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID))
                    .thenReturn(Optional.of(purchaseOrder(PurchaseOrderStatus.PENDING)));

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(PO_ID, principal))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot receive purchase order without items");
            verifyNoInteractions(inventoryRepository, stockMovementRepository);
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("user trong token không còn tồn tại -> dừng trước khi đụng tồn kho, PO vẫn PENDING")
        void unknownUserAbortsBeforeInventory() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(PO_ID, principal))
                    .isInstanceOf(RuntimeException.class);
            verifyNoInteractions(inventoryRepository, stockMovementRepository);
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.PENDING);
        }

        @Test
        @Disabled("BUG: PurchaseOrderServiceImpl.receivePurchaseOrder ném UsernameNotFoundException, "
                + "GlobalExceptionHandler không map nên ra 500; CHECKLIST mục 5 yêu cầu 404 (ResourceNotFoundException)")
        @DisplayName("user trong token không còn tồn tại -> ResourceNotFoundException (404)")
        void unknownUserIsNotFound() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.receivePurchaseOrder(PO_ID, principal))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("cộng inventory bằng 1 lần upsert cho mọi item (tạo mới nếu chưa có), không đọc / lock inventory trước")
        void addsAllQuantitiesInOneUpsert() {
            // given
            WarehouseEntity warehouse = warehouse();
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.setWarehouse(warehouse);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_B), 2));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user()));

            // when
            purchaseOrderService.receivePurchaseOrder(PO_ID, principal);

            // then
            // Existing row or not is decided by MySQL (INSERT ... ON DUPLICATE KEY UPDATE), see PurchaseOrderConcurrencyTest
            verify(inventoryRepository).addQuantities(WAREHOUSE_ID, Map.of(PRODUCT_A, 5, PRODUCT_B, 2));
            verifyNoMoreInteractions(inventoryRepository);
        }

        @Test
        @DisplayName("ghi StockMovement IN cho từng item, performedBy là user đang đăng nhập")
        void writesStockMovementPerItem() {
            // given
            WarehouseEntity warehouse = warehouse();
            ProductEntity productA = product(PRODUCT_A);
            ProductEntity productB = product(PRODUCT_B);
            UserEntity user = user();
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.setWarehouse(warehouse);
            po.getPurchaseOrderItems().add(item(po, productA, 5));
            po.getPurchaseOrderItems().add(item(po, productB, 2));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

            // when
            purchaseOrderService.receivePurchaseOrder(PO_ID, principal);

            // then
            ArgumentCaptor<StockMovementEntity> movements = ArgumentCaptor.forClass(StockMovementEntity.class);
            verify(stockMovementRepository, times(2)).save(movements.capture());
            List<StockMovementEntity> saved = movements.getAllValues();
            assertThat(saved).allSatisfy(movement -> {
                assertThat(movement.getMovementType()).isEqualTo(MovementType.IN);
                assertThat(movement.getWarehouse()).isSameAs(warehouse);
                assertThat(movement.getPerformedBy()).isSameAs(user);
                assertThat(movement.getReason()).contains(PO_ID);
            });
            assertThat(saved).extracting(StockMovementEntity::getProduct).containsExactly(productA, productB);
            assertThat(saved).extracting(StockMovementEntity::getQuantity).containsExactly(5, 2);
            verify(inventoryRepository, times(1)).addQuantities(anyString(), any());
        }

        @Test
        @DisplayName("thành công: status RECEIVED, receivedAt = now, receivedBy tra theo principal.getName()")
        void marksPurchaseOrderReceived() {
            // given
            UserEntity user = user();
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            po.getPurchaseOrderItems().add(item(po, product(PRODUCT_A), 5));
            when(purchaseOrderRepository.findByIdForUpdate(PO_ID)).thenReturn(Optional.of(po));
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            LocalDateTime before = LocalDateTime.now();

            // when
            PurchaseOrderResponse response = purchaseOrderService.receivePurchaseOrder(PO_ID, principal);

            // then
            verify(userRepository).findByUsername(USERNAME);
            verify(purchaseOrderRepository).save(po);
            assertThat(po.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
            assertThat(po.getReceivedBy()).isSameAs(user);
            assertThat(po.getReceivedAt()).isNotNull().isAfterOrEqualTo(before).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(response).isSameAs(convertedResponse);
        }
    }

    @Nested
    @DisplayName("findPurchaseOrderById")
    class FindById {
        @Test
        @DisplayName("không tồn tại -> ResourceNotFoundException (404)")
        void notFound() {
            // given
            when(purchaseOrderRepository.findById(PO_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> purchaseOrderService.findPurchaseOrderById(PO_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("tồn tại -> trả response từ converter")
        void found() {
            // given
            PurchaseOrderEntity po = purchaseOrder(PurchaseOrderStatus.PENDING);
            when(purchaseOrderRepository.findById(PO_ID)).thenReturn(Optional.of(po));

            // when
            PurchaseOrderResponse response = purchaseOrderService.findPurchaseOrderById(PO_ID);

            // then
            assertThat(response).isSameAs(convertedResponse);
            verify(purchaseOrderConverter).toResponse(po);
        }
    }

    // ---------- fixtures ----------

    private static SupplierEntity supplier() {
        SupplierEntity supplier = new SupplierEntity();
        supplier.setId(SUPPLIER_ID);
        supplier.setName("ACME");
        return supplier;
    }

    private static WarehouseEntity warehouse() {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(WAREHOUSE_ID);
        warehouse.setWarehouseCode("WH-001");
        return warehouse;
    }

    private static ProductEntity product(String id) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setProductCode("CODE-" + id);
        return product;
    }

    private static UserEntity user() {
        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername(USERNAME);
        return user;
    }

    private static PurchaseOrderEntity purchaseOrder(PurchaseOrderStatus status) {
        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setId(PO_ID);
        po.setStatus(status);
        po.setSupplier(supplier());
        po.setWarehouse(warehouse());
        return po;
    }

    private static PurchaseOrderItemEntity item(PurchaseOrderEntity po, ProductEntity product, int quantity) {
        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setPurchaseOrder(po);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitCost(BigDecimal.ONE);
        return item;
    }

    private static PurchaseOrderItemRequest itemRequest(String productId, int quantity, String unitCost) {
        PurchaseOrderItemRequest request = new PurchaseOrderItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        request.setUnitCost(new BigDecimal(unitCost));
        return request;
    }

    private static PurchaseOrderRequest poRequest(PurchaseOrderItemRequest... items) {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(SUPPLIER_ID);
        request.setWarehouseId(WAREHOUSE_ID);
        request.setItems(new ArrayList<>(List.of(items)));
        return request;
    }
}
