package com.hung.wms.model.response.purchaseorder;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderResponse {
    private String id, supplierId, warehouseId, status;
    private LocalDateTime createdAt;
    private List<PurchaseOrderItemResponse> items = new ArrayList<>();
}
