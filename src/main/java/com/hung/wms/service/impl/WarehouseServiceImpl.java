package com.hung.wms.service.impl;

import com.hung.wms.converter.WarehouseConverter;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.ResourceDuplicateException;
import com.hung.wms.exception.ResourceInUseException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.response.warehouse.WarehouseResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.repository.entity.WarehouseEntity;
import com.hung.wms.service.WarehouseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class WarehouseServiceImpl implements WarehouseService {
    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private WarehouseConverter warehouseConverter;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.existsByWarehouseCode(request.getWarehouseCode()))
            throw new ResourceDuplicateException("Warehouse already exists");
        WarehouseEntity warehouse = warehouseConverter.toEntity(request);
        return warehouseConverter.toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(String id, WarehouseRequest request) {
        WarehouseEntity warehouse = warehouseRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        modelMapper.map(request, warehouse);
        return warehouseConverter.toResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public List<WarehouseResponse> findAllWarehouses() {
        List<WarehouseEntity> warehouseEntityList = warehouseRepository.findAllByIsDeletedFalse();
        List<WarehouseResponse> warehouseResponseList = new ArrayList<>();
        for (WarehouseEntity items : warehouseEntityList) {
            warehouseResponseList.add(warehouseConverter.toResponse(items));
        }
        return warehouseResponseList;
    }

    @Override
    public WarehouseResponse findWarehouseById(String id) {
        WarehouseEntity warehouse = warehouseRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        return warehouseConverter.toResponse(warehouse);
    }

    @Override
    @Transactional
    public void deleteWarehouseById(String id) {
        WarehouseEntity warehouse = warehouseRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        if (inventoryRepository.existsByWarehouse_IdAndQuantityGreaterThan(id, 0))
            throw new ResourceInUseException("Cannot delete warehouse " + id + ": it still has stock in inventory");
        if (purchaseOrderRepository.existsByWarehouse_IdAndStatus(id, PurchaseOrderStatus.PENDING))
            throw new ResourceInUseException("Cannot delete warehouse " + id + ": it has a pending purchase order");
        warehouse.setIsDeleted(true);
        warehouseRepository.save(warehouse);
    }
}
