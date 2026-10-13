package com.hung.wms.api;

import com.hung.wms.model.request.inventory.InventorySearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.inventory.InventoryResponse;
import com.hung.wms.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryAPI {
    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<PageResponse<InventoryResponse>> getInventory(
            @Valid InventorySearchRequest inventorySearchRequest,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<InventoryResponse> result = inventoryService.findAllInventories(inventorySearchRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
