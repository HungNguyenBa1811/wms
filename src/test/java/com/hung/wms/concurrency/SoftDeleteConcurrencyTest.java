package com.hung.wms.concurrency;

import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.repository.entity.SupplierEntity;
import com.hung.wms.repository.entity.WarehouseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Race giữa "check còn được dùng rồi set isDeleted" và "tạo PO / add item dùng chính bản ghi đó".
 * Invariant: không bao giờ có bản ghi đã xóa mềm mà vẫn nằm trong PO PENDING.
 */
@DisplayName("Xóa mềm song song với tạo PO / add item")
class SoftDeleteConcurrencyTest extends ConcurrencyTestSupport {
    private WarehouseEntity warehouse;
    private ProductEntity product;
    private SupplierEntity supplier;

    @BeforeEach
    void setUp() {
        warehouse = newWarehouse();
        product = newProduct();
        supplier = newSupplier();
    }

    // ---------- Product: đã fix bằng findByIdAndIsDeletedFalseForUpdate ----------

    @Test
    @DisplayName("Add item đang chạy thì xóa product: xóa chờ lock rồi bị 409, product không bị xóa")
    void addItemThenDeleteProduct_deleteWaits_thenInUse() {
        String po = newEmptyPurchaseOrder(supplier.getId(), warehouse.getId());

        HeldTransaction adding = holdTransaction(() -> addItem(po, product.getId(), 3));
        Future<Void> deleting = async(() -> productService.deleteProductById(product.getId()));

        assertBlocked(deleting);
        assertThat(adding.commit()).isNull();
        assertThat(outcome(deleting)).isInstanceOf(ResourceInUseException.class);

        assertThat(isProductDeleted()).isFalse();
        assertThat(itemCount(po)).isEqualTo(1);
    }

    @Test
    @DisplayName("Xóa product đang chạy thì add item: add item chờ lock rồi bị 404, PO không có item")
    void deleteProductThenAddItem_addItemWaits_thenNotFound() {
        String po = newEmptyPurchaseOrder(supplier.getId(), warehouse.getId());

        HeldTransaction deleting = holdTransaction(() -> productService.deleteProductById(product.getId()));
        Future<Void> adding = async(() -> addItem(po, product.getId(), 3));

        assertBlocked(adding);
        assertThat(deleting.commit()).isNull();
        assertThat(outcome(adding)).isInstanceOf(ResourceNotFoundException.class);

        assertThat(isProductDeleted()).isTrue();
        assertThat(itemCount(po)).isZero();
    }

    @Test
    @DisplayName("Tạo PO có product đang chạy thì xóa product: xóa chờ lock rồi bị 409")
    void createPurchaseOrderThenDeleteProduct_deleteWaits_thenInUse() {
        HeldTransaction creating = holdTransaction(() ->
                newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 3));
        Future<Void> deleting = async(() -> productService.deleteProductById(product.getId()));

        assertBlocked(deleting);
        assertThat(creating.commit()).isNull();
        assertThat(outcome(deleting)).isInstanceOf(ResourceInUseException.class);
        assertThat(isProductDeleted()).isFalse();
    }

    @Test
    @DisplayName("Xóa product đang chạy thì tạo PO có product đó: tạo PO chờ lock rồi bị 404")
    void deleteProductThenCreatePurchaseOrder_createWaits_thenNotFound() {
        HeldTransaction deleting = holdTransaction(() -> productService.deleteProductById(product.getId()));
        Future<String> creating = async(() ->
                newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 3));

        assertBlocked(creating);
        assertThat(deleting.commit()).isNull();
        assertThat(outcome(creating)).isInstanceOf(ResourceNotFoundException.class);
        assertThat(purchaseOrderRepository.existsByPurchaseOrderItems_Product_IdAndStatus(
                product.getId(), PurchaseOrderStatus.PENDING)).isFalse();
    }

    // ---------- Warehouse / Supplier: chưa có lock, còn khe hở ----------

    @Test
    @Disabled("Chưa fix: deleteWarehouseById và createPurchaseOrder đọc warehouse không lock, "
            + "cả hai cùng commit -> warehouse đã xóa nhưng vẫn có PO PENDING. Bỏ @Disabled khi bắt đầu sửa.")
    @DisplayName("[Chưa fix] Xóa warehouse song song với tạo PO vào warehouse đó: không được cùng thành công")
    void deleteWarehouseAndCreatePurchaseOrder_mustNotBothSucceed() {
        HeldTransaction deleting = holdTransaction(() -> warehouseService.deleteWarehouseById(warehouse.getId()));
        Future<String> creating = async(() -> newEmptyPurchaseOrder(supplier.getId(), warehouse.getId()));

        letItRun(creating);
        Throwable deleteResult = deleting.commit();
        Throwable createResult = outcome(creating);

        assertThat(deleteResult == null && createResult == null)
                .as("delete (%s) and create PO (%s) both succeeded", deleteResult, createResult)
                .isFalse();
        boolean deleted = warehouseRepository.findById(warehouse.getId()).orElseThrow().getIsDeleted();
        boolean hasPendingPo = purchaseOrderRepository.existsByWarehouse_IdAndStatus(warehouse.getId(), PurchaseOrderStatus.PENDING);
        assertThat(deleted && hasPendingPo).as("deleted warehouse still has a PENDING purchase order").isFalse();
    }

    @Test
    @Disabled("Chưa fix: deleteSupplierById và createPurchaseOrder đọc supplier không lock, "
            + "cả hai cùng commit -> supplier đã xóa nhưng vẫn có PO PENDING. Bỏ @Disabled khi bắt đầu sửa.")
    @DisplayName("[Chưa fix] Xóa supplier song song với tạo PO của supplier đó: không được cùng thành công")
    void deleteSupplierAndCreatePurchaseOrder_mustNotBothSucceed() {
        HeldTransaction deleting = holdTransaction(() -> supplierService.deleteSupplierById(supplier.getId()));
        Future<String> creating = async(() -> newEmptyPurchaseOrder(supplier.getId(), warehouse.getId()));

        letItRun(creating);
        Throwable deleteResult = deleting.commit();
        Throwable createResult = outcome(creating);

        assertThat(deleteResult == null && createResult == null)
                .as("delete (%s) and create PO (%s) both succeeded", deleteResult, createResult)
                .isFalse();
        boolean deleted = supplierRepository.findById(supplier.getId()).orElseThrow().getIsDeleted();
        boolean hasPendingPo = purchaseOrderRepository.existsBySupplier_IdAndStatus(supplier.getId(), PurchaseOrderStatus.PENDING);
        assertThat(deleted && hasPendingPo).as("deleted supplier still has a PENDING purchase order").isFalse();
    }

    private boolean isProductDeleted() {
        return productRepository.findById(product.getId()).orElseThrow().getIsDeleted();
    }
}
