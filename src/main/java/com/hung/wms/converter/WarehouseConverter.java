package com.hung.wms.converter;

import com.hung.wms.model.request.warehouse.WarehouseRequest;
import com.hung.wms.model.response.warehouse.WarehouseResponse;
import com.hung.wms.repository.entity.WarehouseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseConverter {
    @Autowired
    private ModelMapper modelMapper;

    public WarehouseEntity toEntity(WarehouseRequest request) {
        return modelMapper.map(request, WarehouseEntity.class);
    }

    public WarehouseResponse toResponse(WarehouseEntity warehouse) {
        return modelMapper.map(warehouse, WarehouseResponse.class);
    }
}
