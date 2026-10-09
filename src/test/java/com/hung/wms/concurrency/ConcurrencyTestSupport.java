package com.hung.wms.concurrency;

import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.request.purchaseorder.ReceivePurchaseOrderRequest;
import com.hung.wms.repository.CategoryRepository;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.StockMovementRepository;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.repository.entity.InventoryEntity;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.repository.entity.SupplierEntity;
import com.hung.wms.repository.entity.WarehouseEntity;
import com.hung.wms.service.ProductService;
import com.hung.wms.service.PurchaseOrderService;
import com.hung.wms.service.SupplierService;
import com.hung.wms.service.WarehouseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:mysql://localhost:3306/wms_test?createDatabaseIfNotExist=true",
                "spring.jpa.show-sql=false"
        }
)
abstract class ConcurrencyTestSupport {
    protected static final String SEED_CATEGORY_ID = "11111111-1111-1111-1111-111111111111";
    protected static final String SEED_USER_ID = "e1e1e1e1-e1e1-e1e1-e1e1-e1e1e1e1e1e1";

    // Longer than innodb_lock_wait_timeout (50s) so a stuck lock shows up as a lock timeout, not a test timeout
    private static final long RESULT_TIMEOUT_SECONDS = 60;
    // How long a thread must stay stuck before we accept it is waiting on a lock
    private static final long BLOCKED_CHECK_MILLIS = 1500;

    @Autowired
    protected PurchaseOrderService purchaseOrderService;

    @Autowired
    protected ProductService productService;

    @Autowired
    protected WarehouseService warehouseService;

    @Autowired
    protected SupplierService supplierService;

    @Autowired
    protected PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected WarehouseRepository warehouseRepository;

    @Autowired
    protected SupplierRepository supplierRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected InventoryRepository inventoryRepository;

    @Autowired
    protected StockMovementRepository stockMovementRepository;

    @Autowired
    protected TransactionTemplate transactionTemplate;

    private ExecutorService executor;
    private final List<HeldTransaction> heldTransactions = new ArrayList<>();

    @BeforeEach
    void startExecutor() {
        executor = Executors.newCachedThreadPool();
    }

    @AfterEach
    void stopExecutor() {
        // Never leave a transaction holding locks if an assertion failed halfway
        heldTransactions.forEach(HeldTransaction::release);
        heldTransactions.clear();
        executor.shutdownNow();
    }

    // ---------- Thread helpers ----------

    protected <T> Future<T> async(Callable<T> task) {
        return executor.submit(task);
    }

    protected Future<Void> async(Runnable task) {
        return executor.submit(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Runs {@code work} inside a transaction and keeps that transaction open (locks held, nothing committed)
     * until {@link HeldTransaction#commit()} is called. Service methods join this outer transaction,
     * so any lock they take stays held. This gives a fixed order: "A got the lock first, then B arrives".
     */
    protected HeldTransaction holdTransaction(Runnable work) {
        CountDownLatch workDone = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Future<Void> result = executor.submit(() -> {
            transactionTemplate.executeWithoutResult(status -> {
                try {
                    work.run();
                } finally {
                    workDone.countDown();
                }
                awaitQuietly(release);
            });
            return null;
        });
        awaitQuietly(workDone);
        if (result.isDone()) {
            // work threw: surface the real error instead of a confusing timeout later
            Throwable error = outcome(result);
            fail("Held transaction failed before holding its locks", error);
        }
        HeldTransaction held = new HeldTransaction(release, result);
        heldTransactions.add(held);
        return held;
    }

    /** Runs the same task on {@code threads} threads released at the same instant. */
    protected List<Future<Void>> runTogether(int threads, Runnable task) {
        return runTogether(Collections.nCopies(threads, task));
    }

    /** Runs each task on its own thread, all released at the same instant. */
    protected List<Future<Void>> runTogether(List<Runnable> tasks) {
        CountDownLatch ready = new CountDownLatch(tasks.size());
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Void>> futures = new ArrayList<>();
        for (Runnable task : tasks) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                awaitQuietly(go);
                task.run();
                return null;
            }));
        }
        awaitQuietly(ready);
        go.countDown();
        return futures;
    }

    /** Returns null if the task succeeded, otherwise the exception it threw. */
    protected static Throwable outcome(Future<?> future) {
        try {
            future.get(RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return null;
        } catch (ExecutionException e) {
            return e.getCause();
        } catch (InterruptedException | TimeoutException e) {
            throw new AssertionError("Task did not finish in " + RESULT_TIMEOUT_SECONDS + "s", e);
        }
    }

    protected static List<Throwable> outcomes(List<? extends Future<?>> futures) {
        List<Throwable> result = new ArrayList<>();
        for (Future<?> future : futures) {
            result.add(outcome(future));
        }
        return result;
    }

    protected static long successCount(List<Throwable> outcomes) {
        return outcomes.stream().filter(o -> o == null).count();
    }

    /** Asserts the task is stuck, which here means it is waiting on a row lock held by another transaction. */
    protected static void assertBlocked(Future<?> future) {
        assertThatThrownBy(() -> future.get(BLOCKED_CHECK_MILLIS, TimeUnit.MILLISECONDS))
                .as("expected the second transaction to wait for the lock")
                .isInstanceOf(TimeoutException.class);
    }

    /**
     * Gives the task time to get as far as it can (finish, or get stuck on a lock) without asserting which.
     * Use before committing a held transaction when the other side must have done its reads first.
     */
    protected static void letItRun(Future<?> future) {
        try {
            future.get(BLOCKED_CHECK_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException ignored) {
            // still running (waiting on a lock) or already failed: both are fine here, outcome() reads it later
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            if (!latch.await(RESULT_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                throw new IllegalStateException("Latch timed out");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected static final class HeldTransaction {
        private final CountDownLatch release;
        private final Future<Void> result;

        private HeldTransaction(CountDownLatch release, Future<Void> result) {
            this.release = release;
            this.result = result;
        }

        /** Lets the transaction commit and returns null on success, otherwise the commit error. */
        public Throwable commit() {
            release();
            return outcome(result);
        }

        private void release() {
            release.countDown();
        }
    }

    // ---------- Fixtures ----------

    protected WarehouseEntity newWarehouse() {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setWarehouseCode("WH-T-" + shortId());
        warehouse.setName("Concurrency test warehouse");
        warehouse.setLocation("Test");
        return warehouseRepository.save(warehouse);
    }

    protected ProductEntity newProduct() {
        ProductEntity product = new ProductEntity();
        product.setProductCode("P-T-" + shortId());
        product.setName("Concurrency test product");
        product.setPrice(BigDecimal.TEN);
        product.setCategory(categoryRepository.findById(SEED_CATEGORY_ID).orElseThrow());
        return productRepository.save(product);
    }

    protected SupplierEntity newSupplier() {
        SupplierEntity supplier = new SupplierEntity();
        supplier.setName("Concurrency test supplier " + shortId());
        return supplierRepository.save(supplier);
    }

    protected String newPurchaseOrder(String supplierId, String warehouseId, String productId, int quantity) {
        return purchaseOrderService.createPurchaseOrder(purchaseOrderRequest(supplierId, warehouseId, productId, quantity)).getId();
    }

    protected String newEmptyPurchaseOrder(String supplierId, String warehouseId) {
        return purchaseOrderService.createPurchaseOrder(purchaseOrderRequest(supplierId, warehouseId, null, 0)).getId();
    }

    protected PurchaseOrderRequest purchaseOrderRequest(String supplierId, String warehouseId, String productId, int quantity) {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId(supplierId);
        request.setWarehouseId(warehouseId);
        if (productId != null)
            request.getItems().add(itemRequest(productId, quantity));
        return request;
    }

    protected PurchaseOrderItemRequest itemRequest(String productId, int quantity) {
        PurchaseOrderItemRequest item = new PurchaseOrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setUnitCost(BigDecimal.ONE);
        return item;
    }

    protected void receive(String purchaseOrderId) {
        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest();
        request.setReceivedBy(SEED_USER_ID);
        purchaseOrderService.receivePurchaseOrder(purchaseOrderId, request);
    }

    protected void addItem(String purchaseOrderId, String productId, int quantity) {
        purchaseOrderService.addPurchaseOrderItem(purchaseOrderId, itemRequest(productId, quantity));
    }

    // ---------- Reads ----------

    protected int inventoryQuantity(String warehouseId, String productId) {
        List<InventoryEntity> rows = inventoryRepository.findByWarehouse_IdAndProduct_Id(warehouseId, productId);
        assertThat(rows).as("inventory rows for one warehouse + product").hasSizeLessThanOrEqualTo(1);
        return rows.isEmpty() ? 0 : rows.get(0).getQuantity();
    }

    protected int stockMovementCount(String warehouseId, String productId) {
        return stockMovementRepository.findByWarehouse_IdAndProduct_IdOrderByCreatedAtDesc(warehouseId, productId).size();
    }

    protected int itemCount(String purchaseOrderId) {
        return purchaseOrderService.findPurchaseOrderById(purchaseOrderId).getItems().size();
    }

    protected String status(String purchaseOrderId) {
        return purchaseOrderService.findPurchaseOrderById(purchaseOrderId).getStatus();
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
