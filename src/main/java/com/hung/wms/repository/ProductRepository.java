package com.hung.wms.repository;

import com.hung.wms.repository.entity.ProductEntity;
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
public interface ProductRepository extends JpaRepository<ProductEntity, String>,
                                           JpaSpecificationExecutor<ProductEntity> {
    List<ProductEntity> findAll();
    Optional<ProductEntity> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);
    boolean existsByProductCode(String productCode);
    boolean existsByProductCodeAndIdNot(String productCode, String id);
    Optional<ProductEntity> findByIdAndIsDeletedFalse(String id);
    boolean existsByCategory_IdAndIsDeletedFalse(String categoryId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id AND p.isDeleted = false")
    Optional<ProductEntity> findByIdAndIsDeletedFalseForUpdate(@Param("id") String id);

    // Create PO: locks every product of the request in 1 query instead of 1 per item.
    // ORDER BY id so two transactions always lock rows in the same order (no deadlock).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id IN :ids AND p.isDeleted = false ORDER BY p.id")
    List<ProductEntity> findAllByIdInAndIsDeletedFalseForUpdate(@Param("ids") Collection<String> ids);
}
