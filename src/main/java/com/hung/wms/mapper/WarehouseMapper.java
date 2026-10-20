package com.hung.wms.mapper;

import com.hung.wms.dto.request.warehouse.WarehouseRequest;
import com.hung.wms.dto.response.warehouse.WarehouseResponse;
import com.hung.wms.entity.WarehouseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WarehouseMapper {
    @Autowired
    private ModelMapper modelMapper;

    public WarehouseEntity toEntity(WarehouseRequest request) {
        return modelMapper.map(request, WarehouseEntity.class);
    }

    public WarehouseResponse toResponse(WarehouseEntity warehouse) {
        return modelMapper.map(warehouse, WarehouseResponse.class);
    }
}
