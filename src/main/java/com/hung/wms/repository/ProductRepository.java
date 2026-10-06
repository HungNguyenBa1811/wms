package com.hung.wms.repository;

import com.hung.wms.repository.entity.ProductEntity;
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
public interface ProductRepository extends JpaRepository<ProductEntity, String>,
                                           JpaSpecificationExecutor<ProductEntity> {
    List<ProductEntity> findAll();
    Optional<ProductEntity> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);
    boolean existsByProductCode(String productCode);
    boolean existsByProductCodeAndIdNot(String productCode, String id);
    Optional<ProductEntity> findByIdAndIsDeletedFalse(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id AND p.isDeleted = false")
    Optional<ProductEntity> findByIdAndIsDeletedFalseForUpdate(@Param("id") String id);
}
