package com.hung.wms.service.impl;

import com.hung.wms.mapper.InventoryMapper;
import com.hung.wms.dto.request.inventory.InventorySearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.inventory.InventoryResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.entity.InventoryEntity;
import com.hung.wms.repository.specification.InventorySpecification;
import com.hung.wms.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> findAllInventories(InventorySearchRequest request, Pageable pageable) {
        Page<InventoryEntity> inventoryEntityPage = inventoryRepository
                .findAll(InventorySpecification.search(request), pageable);
        return new PageResponse<>(inventoryEntityPage.map(inventoryMapper::toResponse));
    }
}
