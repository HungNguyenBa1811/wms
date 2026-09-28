package com.hung.wms.repository;

import com.hung.wms.repository.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, String> {
    List<PurchaseOrderEntity> findAll();
    Optional<PurchaseOrderEntity> findById(String id);
}
