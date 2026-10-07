package com.hung.wms.concurrency;

import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.InvalidStateException;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.repository.entity.SupplierEntity;
import com.hung.wms.repository.entity.WarehouseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Purchase order: receive / add item song song")
class PurchaseOrderConcurrencyTest extends ConcurrencyTestSupport {
    private WarehouseEntity warehouse;
    private ProductEntity product;
    private SupplierEntity supplier;

    @BeforeEach
    void setUp() {
        // Fresh warehouse + product per test so inventory numbers never mix between tests
        warehouse = newWarehouse();
        product = newProduct();
        supplier = newSupplier();
    }

    // ---------- Receive cùng 1 PO (lock PO) ----------

    @Test
    @DisplayName("Receive cùng 1 PO: request thứ hai phải chờ lock, sau đó nhận 409, kho chỉ cộng 1 lần")
    void receiveSamePurchaseOrder_secondWaitsForLock_thenInvalidState() {
        String po = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 5);

        HeldTransaction first = holdTransaction(() -> receive(po));
        Future<Void> second = async(() -> receive(po));

        assertBlocked(second);
        assertThat(first.commit()).isNull();
        assertThat(outcome(second)).isInstanceOf(InvalidStateException.class);

        assertThat(status(po)).isEqualTo("RECEIVED");
        assertThat(inventoryQuantity(warehouse.getId(), product.getId())).isEqualTo(5);
        assertThat(stockMovementCount(warehouse.getId(), product.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Receive cùng 1 PO từ 10 thread cùng lúc: đúng 1 thành công, 9 cái còn lại 409")
    void receiveSamePurchaseOrder_manyThreads_onlyOneSucceeds() {
        String po = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 5);

        List<Throwable> results = outcomes(runTogether(10, () -> receive(po)));

        assertThat(successCount(results)).isEqualTo(1);
        assertThat(results).filteredOn(r -> r != null).allMatch(r -> r instanceof InvalidStateException);
        assertThat(inventoryQuantity(warehouse.getId(), product.getId())).isEqualTo(5);
        assertThat(stockMovementCount(warehouse.getId(), product.getId())).isEqualTo(1);
    }

    // ---------- Receive nhiều PO khác nhau, cùng warehouse + product (lock inventory) ----------

    @Test
    @DisplayName("Receive 2 PO khác nhau cùng warehouse + product (inventory đã có): PO thứ hai chờ lock inventory, không mất số")
    void receiveDifferentPurchaseOrders_existingInventory_secondWaitsForInventoryLock() {
        receive(newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 1));
        String poA = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 10);
        String poB = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 20);

        HeldTransaction first = holdTransaction(() -> receive(poA));
        Future<Void> second = async(() -> receive(poB));

        assertBlocked(second);
        assertThat(first.commit()).isNull();
        assertThat(outcome(second)).isNull();

        assertThat(inventoryQuantity(warehouse.getId(), product.getId())).isEqualTo(1 + 10 + 20);
    }

    @Test
    @DisplayName("Receive 8 PO khác nhau cùng lúc (inventory đã có): tồn kho = tổng, không lost update")
    void receiveDifferentPurchaseOrders_existingInventory_manyThreads_noLostUpdate() {
        receive(newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 1));
        int threads = 8;
        List<Runnable> receives = new ArrayList<>();
        int expected = 1;
        for (int i = 1; i <= threads; i++) {
            String po = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), i);
            receives.add(() -> receive(po));
            expected += i;
        }

        List<Throwable> results = outcomes(runTogether(receives));
        assertThat(results).containsOnlyNulls();
        assertThat(inventoryQuantity(warehouse.getId(), product.getId())).isEqualTo(expected);
        assertThat(stockMovementCount(warehouse.getId(), product.getId())).isEqualTo(1 + threads);
    }

    @Test
    @Disabled("Chưa fix (CHECKLIST mục 1): inventory chưa có thì SELECT FOR UPDATE không lock được dòng chưa tồn tại, "
            + "2 transaction cùng INSERT -> deadlock hoặc trùng unique -> 500. Bỏ @Disabled khi bắt đầu sửa.")
    @DisplayName("[Chưa fix] Receive 2 PO khác nhau khi inventory chưa có: cả hai phải thành công, tồn kho = tổng")
    void receiveDifferentPurchaseOrders_inventoryNotCreatedYet_bothSucceed() {
        String poA = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 10);
        String poB = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 20);

        HeldTransaction first = holdTransaction(() -> receive(poA));
        Future<Void> second = async(() -> receive(poB));

        letItRun(second);
        Throwable firstResult = first.commit();
        Throwable secondResult = outcome(second);

        assertThat(firstResult).as("first receive").isNull();
        assertThat(secondResult).as("second receive").isNull();
        assertThat(inventoryQuantity(warehouse.getId(), product.getId())).isEqualTo(30);
    }

    // ---------- Add item (lock PO + check trùng product) ----------

    @Test
    @DisplayName("Add cùng 1 product vào cùng 1 PO: request thứ hai chờ lock rồi bị 400 trùng product")
    void addSameProductToSamePurchaseOrder_secondWaitsForLock_thenDuplicate() {
        String po = newEmptyPurchaseOrder(supplier.getId(), warehouse.getId());

        HeldTransaction first = holdTransaction(() -> addItem(po, product.getId(), 3));
        Future<Void> second = async(() -> addItem(po, product.getId(), 4));

        assertBlocked(second);
        assertThat(first.commit()).isNull();
        assertThat(outcome(second)).isInstanceOf(BadRequestException.class);
        assertThat(itemCount(po)).isEqualTo(1);
    }

    @Test
    @DisplayName("Add cùng 1 product vào cùng 1 PO từ 8 thread: PO chỉ có 1 item")
    void addSameProductToSamePurchaseOrder_manyThreads_onlyOneItem() {
        String po = newEmptyPurchaseOrder(supplier.getId(), warehouse.getId());

        List<Throwable> results = outcomes(runTogether(8, () -> addItem(po, product.getId(), 3)));

        assertThat(successCount(results)).isEqualTo(1);
        assertThat(results).filteredOn(r -> r != null).allMatch(r -> r instanceof BadRequestException);
        assertThat(itemCount(po)).isEqualTo(1);
    }

    // ---------- Add item vs receive trên cùng PO ----------

    @Test
    @DisplayName("Receive đang chạy thì add item: add item chờ lock rồi bị 409, không có item lọt vào PO đã RECEIVED")
    void receiveThenAddItem_addItemWaits_thenInvalidState() {
        String po = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 5);
        ProductEntity another = newProduct();

        HeldTransaction receiving = holdTransaction(() -> receive(po));
        Future<Void> adding = async(() -> addItem(po, another.getId(), 7));

        assertBlocked(adding);
        assertThat(receiving.commit()).isNull();
        assertThat(outcome(adding)).isInstanceOf(InvalidStateException.class);

        assertThat(itemCount(po)).isEqualTo(1);
        assertThat(inventoryQuantity(warehouse.getId(), another.getId())).isZero();
    }

    @Test
    @DisplayName("Add item đang chạy thì receive: receive chờ lock rồi nhập kho cả item vừa thêm")
    void addItemThenReceive_receiveWaits_thenIncludesNewItem() {
        String po = newPurchaseOrder(supplier.getId(), warehouse.getId(), product.getId(), 5);
        ProductEntity another = newProduct();

        HeldTransaction adding = holdTransaction(() -> addItem(po, another.getId(), 7));
        Future<Void> receiving = async(() -> receive(po));

        assertBlocked(receiving);
        assertThat(adding.commit()).isNull();
        assertThat(outcome(receiving)).isNull();

        assertThat(inventoryQuantity(warehouse.getId(), product.getId())).isEqualTo(5);
        assertThat(inventoryQuantity(warehouse.getId(), another.getId())).isEqualTo(7);
    }
}
