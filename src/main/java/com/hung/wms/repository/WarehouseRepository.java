package com.hung.wms.repository;

import com.hung.wms.repository.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, String> {
    List<WarehouseEntity> findAll();
    Optional<WarehouseEntity> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);
    boolean existsByWarehouseCode(String warehouseCode);
    List<WarehouseEntity> findAllByIsDeletedFalse();
    Optional<WarehouseEntity> findByIdAndIsDeletedFalse(String id);
}
