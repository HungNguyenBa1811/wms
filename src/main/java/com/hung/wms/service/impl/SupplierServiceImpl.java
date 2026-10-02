package com.hung.wms.service.impl;

import com.hung.wms.converter.SupplierConverter;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.supplier.SupplierRequest;
import com.hung.wms.model.response.supplier.SupplierResponse;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.entity.SupplierEntity;
import com.hung.wms.service.SupplierService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierConverter supplierConverter;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        SupplierEntity supplier = supplierConverter.toEntity(request);
        return supplierConverter.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(String id, SupplierRequest request) {
        SupplierEntity supplier = supplierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        modelMapper.map(request, supplier);
        return supplierConverter.toResponse(supplierRepository.save(supplier));
    }

    @Override
    public List<SupplierResponse> findAllSuppliers() {
        List<SupplierEntity> supplierEntityList = supplierRepository.findAll();
        List<SupplierResponse> supplierResponseList = new ArrayList<>();
        for (SupplierEntity items : supplierEntityList) {
            supplierResponseList.add(supplierConverter.toResponse(items));
        }
        return supplierResponseList;
    }

    @Override
    public SupplierResponse findSupplierById(String id) {
        SupplierEntity supplier = supplierRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
        return supplierConverter.toResponse(supplier);
    }

    @Override
    @Transactional
    public void deleteSupplierById(String id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }
}
