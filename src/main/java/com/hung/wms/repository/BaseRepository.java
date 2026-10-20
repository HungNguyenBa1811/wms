package com.hung.wms.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Common base of every repository: CRUD (JpaRepository) + filter / paging by Specification + row lock by id.
 * {@code @NoRepositoryBean}: Spring Data does not create a bean for this interface itself, only for the
 * concrete XxxRepository extending it.
 * {@code #{#entityName}} is replaced by the entity name of the concrete repository (ProductEntity, ...).
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    // SELECT ... FOR UPDATE: the row stays locked until the transaction ends (PO add item / receive)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdForUpdate(@Param("id") ID id);
}
