package com.hung.wms.service.impl;

import com.hung.wms.converter.InventoryConverter;
import com.hung.wms.model.response.inventory.InventoryResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.entity.InventoryEntity;
import com.hung.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryConverter inventoryConverter;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> findAllInventories(String warehouseId, String productId) {
        List<InventoryEntity> inventoryEntityList;
        if (warehouseId != null && productId != null) {
            inventoryEntityList = inventoryRepository.findByWarehouse_IdAndProduct_Id(warehouseId, productId);
        } else if (warehouseId != null) {
            inventoryEntityList = inventoryRepository.findByWarehouse_Id(warehouseId);
        } else if (productId != null) {
            inventoryEntityList = inventoryRepository.findByProduct_Id(productId);
        } else {
            inventoryEntityList = inventoryRepository.findAll();
        }

        List<InventoryResponse> inventoryResponseList = new ArrayList<>();
        for (InventoryEntity items : inventoryEntityList) {
            inventoryResponseList.add(inventoryConverter.toResponse(items));
        }
        return inventoryResponseList;
    }
}
