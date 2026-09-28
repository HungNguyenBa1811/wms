package com.hung.wms.service.impl;

import com.hung.wms.converter.SupplierConverter;
import com.hung.wms.model.request.supplier.SupplierRequest;
import com.hung.wms.model.response.supplier.SupplierResponse;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.entity.SupplierEntity;
import com.hung.wms.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierConverter supplierConverter;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        SupplierEntity supplier = supplierConverter.toEntity(request);
        return supplierConverter.toResponse(supplierRepository.createSupplier(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(String id, SupplierRequest request) {
        SupplierEntity supplier = supplierConverter.toEntity(request);
        supplier.setId(id);
        return supplierConverter.toResponse(supplierRepository.updateSupplier(supplier));
    }
}
