package com.hung.wms.service;

import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.response.warehouse.WarehouseResponse;

import java.util.List;

public interface WarehouseService {
    WarehouseResponse createWarehouse(WarehouseRequest request);
    WarehouseResponse updateWarehouse(String id, WarehouseRequest request);
    List<WarehouseResponse> findAllWarehouses();
    WarehouseResponse findWarehouseById(String id);
    void deleteWarehouseById(String id);
}
