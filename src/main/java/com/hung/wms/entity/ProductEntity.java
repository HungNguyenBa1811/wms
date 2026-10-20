package com.hung.wms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "products",
        indexes = {
                // GET /products: WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT n reads the index in order, no filesort
                @Index(name = "idx_product_deleted_created", columnList = "is_deleted, created_at"),
                // GET /products?expiryBefore=...: range scan on expiry_date among non-deleted products
                @Index(name = "idx_product_deleted_expiry", columnList = "is_deleted, expiry_date")
        }
)
public class ProductEntity extends SoftDeleteEntity {
    @Column(name = "product_code", unique = true, nullable = false)
    private String productCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    // Import date (ngày nhập), set to today on create when the client does not send it.
    // Different from createdAt (when the row was created in the system): goods can be entered after they arrived
    @Column(name = "imported_at", nullable = false)
    private LocalDate importedAt;

    // Expiry date (hạn sử dụng), null = does not expire. Never before importedAt
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @OneToMany(mappedBy = "product")
    private List<InventoryEntity> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<PurchaseOrderItemEntity> purchaseOrderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<StockMovementEntity> stockMovements = new ArrayList<>();
}
