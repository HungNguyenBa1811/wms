package com.hung.wms.service;

import com.hung.wms.dto.request.stockmovement.StockMovementSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.stockmovement.StockMovementResponse;
import org.springframework.data.domain.Pageable;

public interface StockMovementService {
    PageResponse<StockMovementResponse> findAllStockMovements(StockMovementSearchRequest request, Pageable pageable);
    StockMovementResponse findStockMovementById(String id);
}
