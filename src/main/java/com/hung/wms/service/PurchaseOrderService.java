package com.hung.wms.service;

import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.request.purchaseorder.ReceivePurchaseOrderRequest;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;

import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request);
    PurchaseOrderResponse addPurchaseOrderItem(String id, PurchaseOrderItemRequest request);
    PurchaseOrderResponse receivePurchaseOrder(String id, ReceivePurchaseOrderRequest request);
    List<PurchaseOrderResponse> findAllPurchaseOrders();
    PurchaseOrderResponse findPurchaseOrderById(String id);
}
