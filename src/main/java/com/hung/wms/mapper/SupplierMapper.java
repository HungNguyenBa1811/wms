package com.hung.wms.mapper;

import com.hung.wms.dto.request.supplier.SupplierRequest;
import com.hung.wms.dto.response.supplier.SupplierResponse;
import com.hung.wms.entity.SupplierEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {
    @Autowired
    private ModelMapper modelMapper;

    public SupplierEntity toEntity(SupplierRequest request) {
        return modelMapper.map(request, SupplierEntity.class);
    }

    public SupplierResponse toResponse(SupplierEntity supplier) {
        return modelMapper.map(supplier, SupplierResponse.class);
    }
}
