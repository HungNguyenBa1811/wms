package com.hung.wms.api;

import com.hung.wms.model.response.stockmovement.StockMovementResponse;
import com.hung.wms.service.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-movements")
public class StockMovementAPI {
    @Autowired
    private StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<List<StockMovementResponse>> getStockMovement(@RequestParam(required = false) String warehouseId,
                                                                        @RequestParam(required = false) String productId) {
        List<StockMovementResponse> result = stockMovementService.findAllStockMovements(warehouseId, productId);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMovementResponse> getStockMovementById(@PathVariable String id) {
        StockMovementResponse result = stockMovementService.findStockMovementById(id);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
