package com.hung.wms.mapper;

import com.hung.wms.dto.response.inventory.InventoryResponse;
import com.hung.wms.entity.InventoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {
    @Autowired
    private ModelMapper modelMapper;

    public InventoryResponse toResponse(InventoryEntity inventory) {
        return modelMapper.map(inventory, InventoryResponse.class);
    }
}
