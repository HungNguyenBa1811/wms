package com.hung.wms.repository.custom.impl;

import com.hung.wms.repository.custom.WarehouseCustomRepository;
import com.hung.wms.repository.entity.WarehouseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class WarehouseCustomRepositoryImpl implements WarehouseCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public WarehouseEntity createWarehouse(WarehouseEntity warehouse) {
        warehouse.setId(null);
        warehouse.setCreatedAt(LocalDateTime.now());
        entityManager.persist(warehouse);
        return warehouse;
    }

    @Override
    @Transactional
    public WarehouseEntity updateWarehouse(WarehouseEntity warehouse) {
        WarehouseEntity warehouseEntity = entityManager.find(WarehouseEntity.class, warehouse.getId());
        warehouseEntity.setWarehouseCode(warehouse.getWarehouseCode());
        warehouseEntity.setName(warehouse.getName());
        warehouseEntity.setLocation(warehouse.getLocation());
        return warehouseEntity;
    }
}
