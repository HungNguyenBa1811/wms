package com.hung.wms.model.request.purchaseorder;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceivePurchaseOrderRequest {
    // TODO JWT: take the user from SecurityContext instead of trusting this field
    @NotBlank(message = "Received by is required")
    private String receivedBy;
}
