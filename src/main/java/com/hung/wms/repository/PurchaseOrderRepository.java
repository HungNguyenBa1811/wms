package com.hung.wms.repository;

import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.repository.entity.PurchaseOrderEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, String> {
    List<PurchaseOrderEntity> findAll();
    Optional<PurchaseOrderEntity> findById(String id);
    boolean existsBySupplier_IdAndStatus(String supplierId, PurchaseOrderStatus status);
    boolean existsByWarehouse_IdAndStatus(String warehouseId, PurchaseOrderStatus status);
    boolean existsByPurchaseOrderItems_Product_IdAndStatus(String productId, PurchaseOrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PurchaseOrderEntity p WHERE p.id = :id")
    Optional<PurchaseOrderEntity> findByIdForUpdate(@Param("id") String id);
}
