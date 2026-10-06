package com.hung.wms.service;

import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.request.warehouse.WarehouseSearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.warehouse.WarehouseResponse;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {
    WarehouseResponse createWarehouse(WarehouseRequest request);
    WarehouseResponse updateWarehouse(String id, WarehouseRequest request);
    PageResponse<WarehouseResponse> findAllWarehouses(WarehouseSearchRequest request, Pageable pageable);
    WarehouseResponse findWarehouseById(String id);
    void deleteWarehouseById(String id);
}
