package com.hung.wms.model.request.purchaseorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {
    @NotBlank(message = "Supplier is required")
    private String supplierId;

    @NotBlank(message = "Warehouse is required")
    private String warehouseId;

    @NotEmpty(message = "Purchase order must have at least one item")
    private List<@NotNull(message = "Item must not be null") @Valid PurchaseOrderItemRequest> items = new ArrayList<>();
}
