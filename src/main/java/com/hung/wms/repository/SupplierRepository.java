package com.hung.wms.repository;

import com.hung.wms.entity.SupplierEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends SoftDeleteRepository<SupplierEntity, String> {
    List<SupplierEntity> findAll();
    Optional<SupplierEntity> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);
}
