package com.hung.wms.repository;

import com.hung.wms.repository.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, String> {
    List<InventoryEntity> findAll();
    Optional<InventoryEntity> findById(String id);
    List<InventoryEntity> findByWarehouse_Id(String warehouseId);
    List<InventoryEntity> findByProduct_Id(String productId);
    List<InventoryEntity> findByWarehouse_IdAndProduct_Id(String warehouseId, String productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.warehouse.id = :warehouseId AND i.product.id = :productId")
    Optional<InventoryEntity> findByWarehouseIdAndProductIdForUpdate(@Param("warehouseId") String warehouseId,
                                                                     @Param("productId") String productId);
}
