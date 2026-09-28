package com.hung.wms.repository;

import com.hung.wms.repository.entity.StockMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovementEntity, String> {
    Optional<StockMovementEntity> findById(String id);
}
