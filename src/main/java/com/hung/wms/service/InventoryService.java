package com.hung.wms.service;

import com.hung.wms.model.request.inventory.InventorySearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.inventory.InventoryResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    PageResponse<InventoryResponse> findAllInventories(InventorySearchRequest request, Pageable pageable);
}
