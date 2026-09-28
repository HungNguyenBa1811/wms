package com.hung.wms.converter;

import com.hung.wms.model.request.supplier.SupplierRequest;
import com.hung.wms.model.response.supplier.SupplierResponse;
import com.hung.wms.repository.entity.SupplierEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SupplierConverter {
    @Autowired
    private ModelMapper modelMapper;

    public SupplierEntity toEntity(SupplierRequest request) {
        return modelMapper.map(request, SupplierEntity.class);
    }

    public SupplierResponse toResponse(SupplierEntity supplier) {
        return modelMapper.map(supplier, SupplierResponse.class);
    }
}
