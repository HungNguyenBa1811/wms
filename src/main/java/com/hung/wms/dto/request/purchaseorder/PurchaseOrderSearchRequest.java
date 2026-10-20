package com.hung.wms.dto.request.purchaseorder;

import com.hung.wms.enums.PurchaseOrderStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseOrderSearchRequest {
    private PurchaseOrderStatus status;
    private String supplierId, warehouseId;
}
