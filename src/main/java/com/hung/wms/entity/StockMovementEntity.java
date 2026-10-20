package com.hung.wms.entity;

import com.hung.wms.enums.MovementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "stock_movements",
        indexes = {
                @Index(name = "idx_stock_movement_warehouse_product_created", columnList = "warehouse_id, product_id, created_at"),
                @Index(name = "idx_stock_movement_product_created", columnList = "product_id, created_at"),
                // GET /stock-movements without filter: ORDER BY created_at DESC LIMIT n
                @Index(name = "idx_stock_movement_created", columnList = "created_at")
        }
)
public class StockMovementEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    // Same person as createdBy (the user who received the PO). Kept because it is a real FK to users
    // (UserEntity.stockMovements) and part of the API (performedBy = user id); createdBy is the plain audit username
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by", nullable = false)
    private UserEntity performedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reason", nullable = false)
    private String reason;
}
