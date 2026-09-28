package com.hung.wms.repository.custom.impl;

import com.hung.wms.repository.custom.SupplierCustomRepository;
import com.hung.wms.repository.entity.SupplierEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

public class SupplierCustomRepositoryImpl implements SupplierCustomRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public SupplierEntity createSupplier(SupplierEntity supplier) {
        supplier.setId(null);
        entityManager.persist(supplier);
        return supplier;
    }

    @Override
    @Transactional
    public SupplierEntity updateSupplier(SupplierEntity supplier) {
        SupplierEntity supplierEntity = entityManager.find(SupplierEntity.class, supplier.getId());
        supplierEntity.setName(supplier.getName());
        supplierEntity.setPhone(supplier.getPhone());
        supplierEntity.setEmail(supplier.getEmail());
        supplierEntity.setAddress(supplier.getAddress());
        return supplierEntity;
    }
}
