package com.hung.wms.service;

import com.hung.wms.model.response.stockmovement.StockMovementResponse;

import java.util.List;

public interface StockMovementService {
    List<StockMovementResponse> findAllStockMovements(String warehouseId, String productId);
    StockMovementResponse findStockMovementById(String id);
}
