package com.hung.wms.api;

import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.request.purchaseorder.ReceivePurchaseOrderRequest;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderAPI {
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getPurchaseOrder() {
        List<PurchaseOrderResponse> result = purchaseOrderService.findAllPurchaseOrders();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable String id) {
        PurchaseOrderResponse result = purchaseOrderService.findPurchaseOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        PurchaseOrderResponse result = purchaseOrderService.createPurchaseOrder(purchaseOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<PurchaseOrderResponse> addPurchaseOrderItem(@PathVariable String id,
                                                                      @RequestBody PurchaseOrderItemRequest purchaseOrderItemRequest) {
        PurchaseOrderResponse result = purchaseOrderService.addPurchaseOrderItem(id, purchaseOrderItemRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(@PathVariable String id,
                                                                      @RequestBody ReceivePurchaseOrderRequest receivePurchaseOrderRequest) {
        PurchaseOrderResponse result = purchaseOrderService.receivePurchaseOrder(id, receivePurchaseOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
