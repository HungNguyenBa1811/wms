package com.hung.wms.service;

import com.hung.wms.model.request.supplier.SupplierRequest;
import com.hung.wms.model.response.supplier.SupplierResponse;

import java.util.List;

public interface SupplierService {
    SupplierResponse createSupplier(SupplierRequest request);
    SupplierResponse updateSupplier(String id, SupplierRequest request);
    List<SupplierResponse> findAllSuppliers();
    SupplierResponse findSupplierById(String id);
    void deleteSupplierById(String id);
}
