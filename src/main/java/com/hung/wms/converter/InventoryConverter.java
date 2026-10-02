package com.hung.wms.converter;

import com.hung.wms.model.response.inventory.InventoryResponse;
import com.hung.wms.repository.entity.InventoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryConverter {
    @Autowired
    private ModelMapper modelMapper;

    public InventoryResponse toResponse(InventoryEntity inventory) {
        return modelMapper.map(inventory, InventoryResponse.class);
    }
}
