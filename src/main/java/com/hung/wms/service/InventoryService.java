package com.hung.wms.service;

import com.hung.wms.dto.request.inventory.InventorySearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.inventory.InventoryResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    PageResponse<InventoryResponse> findAllInventories(InventorySearchRequest request, Pageable pageable);
}
