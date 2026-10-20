package com.hung.wms.service;

import com.hung.wms.dto.request.supplier.SupplierRequest;
import com.hung.wms.dto.request.supplier.SupplierSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.supplier.SupplierResponse;
import org.springframework.data.domain.Pageable;

public interface SupplierService {
    SupplierResponse createSupplier(SupplierRequest request);
    SupplierResponse updateSupplier(String id, SupplierRequest request);
    PageResponse<SupplierResponse> findAllSuppliers(SupplierSearchRequest request, Pageable pageable);
    SupplierResponse findSupplierById(String id);
    void deleteSupplierById(String id);
}
