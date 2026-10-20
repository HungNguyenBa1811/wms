package com.hung.wms.repository;

import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.entity.PurchaseOrderEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, String>, JpaSpecificationExecutor<PurchaseOrderEntity> {
    @Override
    @EntityGraph(attributePaths = {"purchaseOrderItems", "receivedBy"})
    Page<PurchaseOrderEntity> findAll(Specification<PurchaseOrderEntity> spec, Pageable pageable);

    List<PurchaseOrderEntity> findAll();
    // GET /purchase-orders/{id}: PO + receivedBy + items in 1 query instead of 3
    @EntityGraph(attributePaths = {"purchaseOrderItems", "receivedBy"})
    Optional<PurchaseOrderEntity> findById(String id);
    boolean existsBySupplier_IdAndStatus(String supplierId, PurchaseOrderStatus status);
    boolean existsByWarehouse_IdAndStatus(String warehouseId, PurchaseOrderStatus status);
    boolean existsByPurchaseOrderItems_Product_IdAndStatus(String productId, PurchaseOrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PurchaseOrderEntity p WHERE p.id = :id")
    Optional<PurchaseOrderEntity> findByIdForUpdate(@Param("id") String id);
}
