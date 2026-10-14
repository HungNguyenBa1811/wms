package com.hung.wms.service;

import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface PurchaseOrderService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    PurchaseOrderResponse addPurchaseOrderItem(String id, PurchaseOrderItemRequest request);
    PurchaseOrderResponse receivePurchaseOrder(String id, Principal principal);
    PageResponse<PurchaseOrderResponse> findAllPurchaseOrders(PurchaseOrderSearchRequest request, Pageable pageable);
    PurchaseOrderResponse findPurchaseOrderById(String id);
}
