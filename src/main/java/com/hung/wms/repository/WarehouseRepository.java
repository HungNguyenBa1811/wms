package com.hung.wms.repository;

import com.hung.wms.entity.WarehouseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends SoftDeleteRepository<WarehouseEntity, String> {
    List<WarehouseEntity> findAll();
    Optional<WarehouseEntity> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);
    boolean existsByWarehouseCodeAndIdNot(String warehouseCode, String id);
    boolean existsByWarehouseCode(String warehouseCode);
}
