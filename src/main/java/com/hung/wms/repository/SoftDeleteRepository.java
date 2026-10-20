package com.hung.wms.repository;

import com.hung.wms.entity.SoftDeleteEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Base of the 4 master-data repositories (Category / Product / Warehouse / Supplier, entities extending
 * SoftDeleteEntity): reads that ignore soft-deleted rows. A separate level from BaseRepository because the other
 * entities have no isDeleted column, so these queries would fail at startup on their repositories.
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteEntity, ID> extends BaseRepository<T, ID> {
    Optional<T> findByIdAndIsDeletedFalse(ID id);

    // Same as above + SELECT ... FOR UPDATE (update / delete product, add item to a PO)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.isDeleted = false")
    Optional<T> findByIdAndIsDeletedFalseForUpdate(@Param("id") ID id);
}
