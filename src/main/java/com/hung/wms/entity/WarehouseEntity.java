package com.hung.wms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "warehouses")
public class WarehouseEntity extends SoftDeleteEntity {
    @Column(name = "warehouse_code", unique = true, nullable = false)
    private String warehouseCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "location", nullable = false)
    private String location;

    @OneToMany(mappedBy = "warehouse")
    private List<InventoryEntity> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "warehouse")
    private List<PurchaseOrderEntity> purchaseOrders = new ArrayList<>();

    @OneToMany(mappedBy = "warehouse")
    private List<StockMovementEntity> stockMovements = new ArrayList<>();
}
