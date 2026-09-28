package com.hung.wms.service;

import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.response.warehouse.WarehouseResponse;

public interface WarehouseService {
    WarehouseResponse createWarehouse(WarehouseRequest request);
    WarehouseResponse updateWarehouse(String id, WarehouseRequest request);
}
