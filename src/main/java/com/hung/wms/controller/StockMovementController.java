package com.hung.wms.controller;

import com.hung.wms.dto.request.stockmovement.StockMovementSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.stockmovement.StockMovementResponse;
import com.hung.wms.service.StockMovementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementController {
    @Autowired
    private StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<PageResponse<StockMovementResponse>> getStockMovement(
            @Valid StockMovementSearchRequest stockMovementSearchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<StockMovementResponse> result = stockMovementService.findAllStockMovements(stockMovementSearchRequest, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovementResponse> getStockMovementById(@PathVariable String id) {
        StockMovementResponse result = stockMovementService.findStockMovementById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
