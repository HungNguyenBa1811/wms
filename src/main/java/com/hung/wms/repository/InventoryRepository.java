package com.hung.wms.repository;

import com.hung.wms.repository.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, String>,
                                             JpaSpecificationExecutor<InventoryEntity> {
    List<InventoryEntity> findAll();
    Optional<InventoryEntity> findById(String id);
    boolean existsByProduct_IdAndQuantityGreaterThan(String productId, Integer quantity);
    boolean existsByWarehouse_IdAndQuantityGreaterThan(String warehouseId, Integer quantity);

    // Receive: locks every inventory row the PO needs in 1 query instead of 1 per item.
    // ORDER BY product id so two transactions always lock rows in the same order (no deadlock).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.warehouse.id = :warehouseId AND i.product.id IN :productIds ORDER BY i.product.id")
    List<InventoryEntity> findAllByWarehouseIdAndProductIdInForUpdate(@Param("warehouseId") String warehouseId,
                                                                      @Param("productIds") Collection<String> productIds);
}
