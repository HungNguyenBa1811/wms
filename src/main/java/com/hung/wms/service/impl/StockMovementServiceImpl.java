package com.hung.wms.service.impl;

import com.hung.wms.converter.StockMovementConverter;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.stockmovement.StockMovementSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.stockmovement.StockMovementResponse;
import com.hung.wms.repository.StockMovementRepository;
import com.hung.wms.repository.entity.StockMovementEntity;
import com.hung.wms.repository.specification.StockMovementSpecification;
import com.hung.wms.service.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMovementServiceImpl implements StockMovementService {
    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private StockMovementConverter stockMovementConverter;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockMovementResponse> findAllStockMovements(StockMovementSearchRequest request, Pageable pageable) {
        Page<StockMovementEntity> stockMovementEntityPage = stockMovementRepository
                .findAll(StockMovementSpecification.search(request), pageable);
        return new PageResponse<>(stockMovementEntityPage.map(stockMovementConverter::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public StockMovementResponse findStockMovementById(String id) {
        StockMovementEntity stockMovement = stockMovementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock movement not found with id: " + id));
        return stockMovementConverter.toResponse(stockMovement);
    }
}
