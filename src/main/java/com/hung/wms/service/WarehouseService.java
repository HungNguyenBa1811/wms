package com.hung.wms.service;

import com.hung.wms.dto.request.warehouse.WarehouseRequest;
import com.hung.wms.dto.request.warehouse.WarehouseSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.warehouse.WarehouseResponse;
import org.springframework.data.domain.Pageable;

public interface WarehouseService {
    WarehouseResponse createWarehouse(WarehouseRequest request);
    WarehouseResponse updateWarehouse(String id, WarehouseRequest request);
    PageResponse<WarehouseResponse> findAllWarehouses(WarehouseSearchRequest request, Pageable pageable);
    WarehouseResponse findWarehouseById(String id);
    void deleteWarehouseById(String id);
}
