package com.hung.wms.dto.response.purchaseorder;

import com.hung.wms.dto.response.user.UserResponse;
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
    // Username of the logged-in user who created the row (JPA auditing), null if created without login
    private String createdBy;
    private LocalDateTime receivedAt;
    private UserResponse receivedBy;
    private List<PurchaseOrderItemResponse> items = new ArrayList<>();
}
