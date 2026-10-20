package com.hung.wms.repository;

import com.hung.wms.entity.StockMovementEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockMovementRepository extends BaseRepository<StockMovementEntity, String> {
    Optional<StockMovementEntity> findById(String id);
}
