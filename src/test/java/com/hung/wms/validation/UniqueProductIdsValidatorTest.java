package com.hung.wms.validation;

import com.hung.wms.dto.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("@UniqueProductIds trên PurchaseOrderRequest.items")
class UniqueProductIdsValidatorTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    @DisplayName("product khác nhau -> hợp lệ")
    void distinctProducts() {
        assertThat(uniqueViolations(request(item("p-1"), item("p-2")))).isEmpty();
    }

    @Test
    @DisplayName("cùng product 2 lần -> 1 lỗi trên field items")
    void duplicateProduct() {
        Set<ConstraintViolation<PurchaseOrderRequest>> violations = uniqueViolations(request(item("p-1"), item("p-2"), item("p-1")));

        assertThat(violations).hasSize(1);
        ConstraintViolation<PurchaseOrderRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath()).hasToString("items");
        assertThat(violation.getMessage()).isEqualTo("Each product can appear only once in a purchase order");
    }

    @Test
    @DisplayName("item null / productId trống không bị tính là trùng (để @NotNull / @NotBlank báo)")
    void ignoresNullAndBlank() {
        assertThat(uniqueViolations(request(null, null, item(" "), item(" "), item(null), item(null)))).isEmpty();
    }

    private Set<ConstraintViolation<PurchaseOrderRequest>> uniqueViolations(PurchaseOrderRequest request) {
        Set<ConstraintViolation<PurchaseOrderRequest>> violations = validator.validate(request);
        violations.removeIf(v -> !(v.getConstraintDescriptor().getAnnotation() instanceof UniqueProductIds));
        return violations;
    }

    private static PurchaseOrderRequest request(PurchaseOrderItemRequest... items) {
        PurchaseOrderRequest request = new PurchaseOrderRequest();
        request.setSupplierId("supplier-1");
        request.setWarehouseId("warehouse-1");
        request.setItems(new ArrayList<>(Arrays.asList(items)));
        return request;
    }

    private static PurchaseOrderItemRequest item(String productId) {
        PurchaseOrderItemRequest item = new PurchaseOrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(1);
        item.setUnitCost(BigDecimal.ONE);
        return item;
    }
}
