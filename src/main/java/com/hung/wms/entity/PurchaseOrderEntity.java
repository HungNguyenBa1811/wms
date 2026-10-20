package com.hung.wms.entity;

import com.hung.wms.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "purchase_orders",
        indexes = {
                // GET /purchase-orders: ORDER BY created_at DESC LIMIT n (also the subquery of the @EntityGraph page query)
                @Index(name = "idx_purchase_order_created", columnList = "created_at"),
                // GET /purchase-orders?status=...: filter + sort from the same index
                @Index(name = "idx_purchase_order_status_created", columnList = "status, created_at")
        }
)
public class PurchaseOrderEntity extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private SupplierEntity supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WarehouseEntity warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseOrderStatus status;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by_id")
    private UserEntity receivedBy;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItemEntity> purchaseOrderItems = new ArrayList<>();
}
