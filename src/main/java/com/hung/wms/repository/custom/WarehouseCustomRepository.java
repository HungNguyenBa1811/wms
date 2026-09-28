package com.hung.wms.repository.custom;

import com.hung.wms.repository.entity.WarehouseEntity;

public interface WarehouseCustomRepository {
    WarehouseEntity createWarehouse(WarehouseEntity warehouse);
    WarehouseEntity updateWarehouse(WarehouseEntity warehouse);
}
