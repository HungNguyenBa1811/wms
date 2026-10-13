package com.hung.wms.service.impl;

import com.hung.wms.converter.InventoryConverter;
import com.hung.wms.model.request.inventory.InventorySearchRequest;
import com.hung.wms.model.response.common.PageResponse;
import com.hung.wms.model.response.inventory.InventoryResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.entity.InventoryEntity;
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
    private InventoryConverter inventoryConverter;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> findAllInventories(InventorySearchRequest request, Pageable pageable) {
        Page<InventoryEntity> inventoryEntityPage = inventoryRepository
                .findAll(InventorySpecification.search(request), pageable);
        return new PageResponse<>(inventoryEntityPage.map(inventoryConverter::toResponse));
    }
}
