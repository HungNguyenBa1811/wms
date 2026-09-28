package com.hung.wms.model.request.purchaseorder;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequest {
    private String supplierId, warehouseId;
    private List<PurchaseOrderItemRequest> items = new ArrayList<>();
}
