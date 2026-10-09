package com.hung.wms.concurrency;

import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.model.request.product.ProductRequest;
import com.hung.wms.model.request.warehouse.WarehouseRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tạo trùng mã song song")
class DuplicateCodeConcurrencyTest extends ConcurrencyTestSupport {

    @Test
    @Disabled("Chưa fix: request thua nhận DataIntegrityViolationException (500) thay vì ResourceDuplicateException (409). "
            + "Bỏ @Disabled khi bắt đầu sửa.")
    @DisplayName("[Chưa fix] 2 request tạo product cùng productCode: 1 thành công, 1 nhận 409")
    void createProductWithSameCode_loserGetsDuplicate() {
        String code = "DUP-" + UUID.randomUUID().toString().substring(0, 8);

        HeldTransaction first = holdTransaction(() -> productService.createProduct(productRequest(code)));
        Future<Void> second = async(() -> {
            productService.createProduct(productRequest(code));
        });

        letItRun(second);
        Throwable firstResult = first.commit();
        Throwable secondResult = outcome(second);

        assertOneWinsOtherDuplicate(firstResult, secondResult);
    }

    @Test
    @Disabled("Chưa fix: request thua nhận DataIntegrityViolationException (500) thay vì ResourceDuplicateException (409). "
            + "Bỏ @Disabled khi bắt đầu sửa.")
    @DisplayName("[Chưa fix] 2 request tạo warehouse cùng warehouseCode: 1 thành công, 1 nhận 409")
    void createWarehouseWithSameCode_loserGetsDuplicate() {
        String code = "DUP-" + UUID.randomUUID().toString().substring(0, 8);

        HeldTransaction first = holdTransaction(() -> warehouseService.createWarehouse(warehouseRequest(code)));
        Future<Void> second = async(() -> {
            warehouseService.createWarehouse(warehouseRequest(code));
        });

        letItRun(second);
        Throwable firstResult = first.commit();
        Throwable secondResult = outcome(second);

        assertOneWinsOtherDuplicate(firstResult, secondResult);
    }

    private static void assertOneWinsOtherDuplicate(Throwable first, Throwable second) {
        assertThat(first == null ^ second == null)
                .as("exactly one request should succeed, got first=%s second=%s", first, second)
                .isTrue();
        Throwable loser = first != null ? first : second;
        assertThat(loser).isInstanceOf(ResourceDuplicateException.class);
    }

    private static ProductRequest productRequest(String code) {
        ProductRequest request = new ProductRequest();
        request.setProductCode(code);
        request.setName("Duplicate test");
        request.setCategoryId(SEED_CATEGORY_ID);
        request.setPrice(BigDecimal.TEN);
        return request;
    }

    private static WarehouseRequest warehouseRequest(String code) {
        WarehouseRequest request = new WarehouseRequest();
        request.setWarehouseCode(code);
        request.setName("Duplicate test");
        request.setLocation("Test");
        return request;
    }
}
