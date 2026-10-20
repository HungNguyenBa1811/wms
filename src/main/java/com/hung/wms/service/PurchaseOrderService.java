package com.hung.wms.service;

import com.hung.wms.dto.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.purchaseorder.PurchaseOrderResponse;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface PurchaseOrderService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    PurchaseOrderResponse addPurchaseOrderItem(String id, PurchaseOrderItemRequest request);
    PurchaseOrderResponse receivePurchaseOrder(String id, Principal principal);
    PageResponse<PurchaseOrderResponse> findAllPurchaseOrders(PurchaseOrderSearchRequest request, Pageable pageable);
    PurchaseOrderResponse findPurchaseOrderById(String id);
}
