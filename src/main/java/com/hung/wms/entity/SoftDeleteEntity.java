package com.hung.wms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * Master data that is soft-deleted (Category / Product / Warehouse / Supplier): DELETE only sets isDeleted = true,
 * so purchase orders, inventory and stock movements still point to an existing row.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeleteEntity extends BaseEntity {
    @ColumnDefault("false")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
