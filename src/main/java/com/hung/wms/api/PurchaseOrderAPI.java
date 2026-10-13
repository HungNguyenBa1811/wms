package com.hung.wms.api;

import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderSearchRequest;
import com.hung.wms.model.request.purchaseorder.ReceivePurchaseOrderRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderAPI {
    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public ResponseEntity<PageResponse<PurchaseOrderResponse>> getPurchaseOrder(
            @Valid PurchaseOrderSearchRequest purchaseOrderSearchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<PurchaseOrderResponse> result = purchaseOrderService.findAllPurchaseOrders(purchaseOrderSearchRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable String id) {
        PurchaseOrderResponse result = purchaseOrderService.findPurchaseOrderById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest purchaseOrderRequest) {
        PurchaseOrderResponse result = purchaseOrderService.createPurchaseOrder(purchaseOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<PurchaseOrderResponse> addPurchaseOrderItem(@PathVariable String id,
                                                                      @Valid @RequestBody PurchaseOrderItemRequest purchaseOrderItemRequest) {
        PurchaseOrderResponse result = purchaseOrderService.addPurchaseOrderItem(id, purchaseOrderItemRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseOrderResponse> receivePurchaseOrder(@PathVariable String id,
                                                                      @Valid @RequestBody ReceivePurchaseOrderRequest receivePurchaseOrderRequest) {
        PurchaseOrderResponse result = purchaseOrderService.receivePurchaseOrder(id, receivePurchaseOrderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
