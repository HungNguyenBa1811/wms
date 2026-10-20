package com.hung.wms.repository;

import com.hung.wms.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, String>,
                                         JpaSpecificationExecutor<CategoryEntity> {
    List<CategoryEntity> findAll();
    Optional<CategoryEntity> findById(String id);
    Optional<CategoryEntity> findByIdAndIsDeletedFalse(String id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
}
