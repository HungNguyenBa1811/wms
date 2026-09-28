package com.hung.wms.service.impl;

import com.hung.wms.converter.WarehouseConverter;
import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.response.warehouse.WarehouseResponse;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.repository.entity.WarehouseEntity;
import com.hung.wms.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WarehouseServiceImpl implements WarehouseService {
    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseConverter warehouseConverter;

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        WarehouseEntity warehouse = warehouseConverter.toEntity(request);
        return warehouseConverter.toResponse(warehouseRepository.createWarehouse(warehouse));
    }

    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(String id, WarehouseRequest request) {
        WarehouseEntity warehouse = warehouseConverter.toEntity(request);
        warehouse.setId(id);
        return warehouseConverter.toResponse(warehouseRepository.updateWarehouse(warehouse));
    }
}
