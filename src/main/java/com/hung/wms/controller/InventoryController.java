package com.hung.wms.controller;

import com.hung.wms.dto.request.inventory.InventorySearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.inventory.InventoryResponse;
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
public class InventoryController {
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
