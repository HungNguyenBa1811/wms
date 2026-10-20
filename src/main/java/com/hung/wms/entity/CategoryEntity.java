package com.hung.wms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "categories",
        indexes = {
                // GET list: WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT n, same as products
                @Index(name = "idx_category_deleted_created", columnList = "is_deleted, created_at")
        }
)
public class CategoryEntity extends SoftDeleteEntity {
    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "category")
    private List<ProductEntity> products = new ArrayList<>();
}
