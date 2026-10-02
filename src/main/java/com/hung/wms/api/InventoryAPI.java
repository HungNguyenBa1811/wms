package com.hung.wms.api;

import com.hung.wms.model.response.inventory.InventoryResponse;
import com.hung.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryAPI {
    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getInventory(@RequestParam(required = false) String warehouseId,
                                                                @RequestParam(required = false) String productId) {
        List<InventoryResponse> result = inventoryService.findAllInventories(warehouseId, productId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
