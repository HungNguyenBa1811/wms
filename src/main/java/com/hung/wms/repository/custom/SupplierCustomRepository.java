package com.hung.wms.repository.custom;

import com.hung.wms.repository.entity.SupplierEntity;

public interface SupplierCustomRepository {
    SupplierEntity createSupplier(SupplierEntity supplier);
    SupplierEntity updateSupplier(SupplierEntity supplier);
}
