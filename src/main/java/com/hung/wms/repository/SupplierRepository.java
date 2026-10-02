package com.hung.wms.repository;

import com.hung.wms.repository.entity.SupplierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, String> {
    List<SupplierEntity> findAll();
    Optional<SupplierEntity> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);
    List<SupplierEntity> findAllByIsDeletedFalse();
    Optional<SupplierEntity> findByIdAndIsDeletedFalse(String id);
}
