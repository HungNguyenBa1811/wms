package com.hung.wms.repository;

import com.hung.wms.repository.custom.InventoryRepositoryCustom;
import com.hung.wms.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, String>,
                                             JpaSpecificationExecutor<InventoryEntity>,
                                             InventoryRepositoryCustom {
    List<InventoryEntity> findAll();
    Optional<InventoryEntity> findById(String id);
    boolean existsByProduct_IdAndQuantityGreaterThan(String productId, Integer quantity);
    boolean existsByWarehouse_IdAndQuantityGreaterThan(String warehouseId, Integer quantity);

    // Receive: addQuantities (InventoryRepositoryCustom) adds every item of the PO in 1 upsert statement,
    // creating missing rows. Replaces findAllByWarehouseIdAndProductIdInForUpdate + INSERT / UPDATE, which raced
    // when the row did not exist yet (nothing to lock).
}
