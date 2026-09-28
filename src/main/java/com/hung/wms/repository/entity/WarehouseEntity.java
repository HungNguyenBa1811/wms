package com.hung.wms.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "warehouses")
public class WarehouseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "warehouse_code", unique = true, nullable = false)
    private String warehouseCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "warehouse")
    private List<InventoryEntity> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "warehouse")
    private List<PurchaseOrderEntity> purchaseOrders = new ArrayList<>();

    @OneToMany(mappedBy = "warehouse")
    private List<StockMovementEntity> stockMovements = new ArrayList<>();
}
