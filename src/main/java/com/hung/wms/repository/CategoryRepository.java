package com.hung.wms.repository;

import com.hung.wms.entity.CategoryEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends SoftDeleteRepository<CategoryEntity, String> {
    List<CategoryEntity> findAll();
    Optional<CategoryEntity> findById(String id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, String id);
}
