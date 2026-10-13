package com.hung.wms.repository;

import com.hung.wms.repository.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, String>,
                                             JpaSpecificationExecutor<InventoryEntity> {
    List<InventoryEntity> findAll();
    Optional<InventoryEntity> findById(String id);
    boolean existsByProduct_IdAndQuantityGreaterThan(String productId, Integer quantity);
    boolean existsByWarehouse_IdAndQuantityGreaterThan(String warehouseId, Integer quantity);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.warehouse.id = :warehouseId AND i.product.id = :productId")
    Optional<InventoryEntity> findByWarehouseIdAndProductIdForUpdate(@Param("warehouseId") String warehouseId,
                                                                     @Param("productId") String productId);
}
