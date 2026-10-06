package com.hung.wms.api;

import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.request.warehouse.WarehouseSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.warehouse.WarehouseResponse;
import com.hung.wms.service.WarehouseService;
import com.hung.wms.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseAPI {
    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<PageResponse<WarehouseResponse>> getWarehouse(
            @Valid WarehouseSearchRequest warehouseSearchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<WarehouseResponse> result = warehouseService.findAllWarehouses(warehouseSearchRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable String id) {
        WarehouseResponse result = warehouseService.findWarehouseById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Validated({OnCreate.class, Default.class}) @RequestBody WarehouseRequest warehouseRequest) {
        WarehouseResponse result = warehouseService.createWarehouse(warehouseRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(@PathVariable String id,
                                                             @Valid @RequestBody WarehouseRequest warehouseRequest) {
        WarehouseResponse result = warehouseService.updateWarehouse(id, warehouseRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable String id) {
        warehouseService.deleteWarehouseById(id);
        return ResponseEntity.noContent().build();
    }
}
