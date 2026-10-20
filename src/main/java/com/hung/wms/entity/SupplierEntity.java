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
        name = "suppliers",
        indexes = {
                // GET list: WHERE is_deleted = 0 ORDER BY created_at DESC LIMIT n, same as products
                @Index(name = "idx_supplier_deleted_created", columnList = "is_deleted, created_at")
        }
)
public class SupplierEntity extends SoftDeleteEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "supplier")
    private List<PurchaseOrderEntity> purchaseOrders = new ArrayList<>();
}
