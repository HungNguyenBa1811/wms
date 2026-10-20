package com.hung.wms.controller;

import com.hung.wms.dto.request.supplier.SupplierRequest;
import com.hung.wms.dto.request.supplier.SupplierSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.supplier.SupplierResponse;
import com.hung.wms.service.SupplierService;
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
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ResponseEntity<PageResponse<SupplierResponse>> getSupplier(
            @Valid SupplierSearchRequest supplierSearchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<SupplierResponse> result = supplierService.findAllSuppliers(supplierSearchRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable String id) {
        SupplierResponse result = supplierService.findSupplierById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @Validated({OnCreate.class, Default.class}) @RequestBody SupplierRequest supplierRequest) {
        SupplierResponse result = supplierService.createSupplier(supplierRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable String id,
                                                           @Valid @RequestBody SupplierRequest supplierRequest) {
        SupplierResponse result = supplierService.updateSupplier(id, supplierRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable String id) {
        supplierService.deleteSupplierById(id);
        return ResponseEntity.noContent().build();
    }
}
