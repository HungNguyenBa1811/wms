package com.hung.wms.service.impl;

import com.hung.wms.converter.StockMovementConverter;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.response.stockmovement.StockMovementResponse;
import com.hung.wms.repository.StockMovementRepository;
import com.hung.wms.repository.entity.StockMovementEntity;
import com.hung.wms.service.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockMovementServiceImpl implements StockMovementService {
    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private StockMovementConverter stockMovementConverter;

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> findAllStockMovements(String warehouseId, String productId) {
        List<StockMovementEntity> stockMovementEntityList;
        if (warehouseId != null && productId != null) {
            stockMovementEntityList = stockMovementRepository
                    .findByWarehouse_IdAndProduct_IdOrderByCreatedAtDesc(warehouseId, productId);
        } else if (warehouseId != null) {
            stockMovementEntityList = stockMovementRepository.findByWarehouse_IdOrderByCreatedAtDesc(warehouseId);
        } else if (productId != null) {
            stockMovementEntityList = stockMovementRepository.findByProduct_IdOrderByCreatedAtDesc(productId);
        } else {
            stockMovementEntityList = stockMovementRepository.findAllByOrderByCreatedAtDesc();
        }

        List<StockMovementResponse> stockMovementResponseList = new ArrayList<>();
        for (StockMovementEntity items : stockMovementEntityList) {
            stockMovementResponseList.add(stockMovementConverter.toResponse(items));
        }
        return stockMovementResponseList;
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
