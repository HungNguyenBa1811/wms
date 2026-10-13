package com.hung.wms.service;

import com.hung.wms.model.request.stockmovement.StockMovementSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.stockmovement.StockMovementResponse;
import org.springframework.data.domain.Pageable;

public interface StockMovementService {
    PageResponse<StockMovementResponse> findAllStockMovements(StockMovementSearchRequest request, Pageable pageable);
    StockMovementResponse findStockMovementById(String id);
}
