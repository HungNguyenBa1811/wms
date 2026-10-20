package com.hung.wms.repository.custom;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

// Found by Spring Data through the "Impl" suffix, no annotation needed
public class InventoryRepositoryCustomImpl implements InventoryRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;

    // Why an upsert instead of SELECT ... FOR UPDATE + INSERT: when the row does not exist yet there is nothing to lock,
    // so two receives of the same (warehouse, product) both INSERT and one fails on uk_inventory_warehouse_product.
    // Here MySQL itself checks the unique key: the first transaction inserts the row, the second waits for its lock,
    // then adds to it (quantity = current value + incoming), so no lost update and no duplicate row.
    // Native SQL: @PrePersist / @UpdateTimestamp do not run, so id and updated_at are set here.
    @Override
    public void addQuantities(String warehouseId, Map<String, Integer> quantityByProductId) {
        if (quantityByProductId.isEmpty()) return;
        // Rows are inserted / locked in VALUES order: sort by product id so every transaction locks in the same order
        // (same rule as the other ORDER BY id FOR UPDATE queries), otherwise two multi-item receives could deadlock
        Map<String, Integer> sorted = new TreeMap<>(quantityByProductId);

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            rows.add("(:id" + i + ", :warehouseId, :productId" + i + ", :quantity" + i + ", :updatedAt)");
        }
        // Row alias "AS incoming" (MySQL 8.0.19+) instead of VALUES(quantity), which is deprecated since 8.0.20
        String sql = "INSERT INTO inventory (id, warehouse_id, product_id, quantity, updated_at) VALUES "
                + String.join(", ", rows)
                + " AS incoming ON DUPLICATE KEY UPDATE"
                + " quantity = inventory.quantity + incoming.quantity, updated_at = incoming.updated_at";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("warehouseId", warehouseId);
        query.setParameter("updatedAt", LocalDateTime.now());
        int i = 0;
        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            // Only used when the row is new; same UUID string format as @GeneratedValue(strategy = UUID)
            query.setParameter("id" + i, UUID.randomUUID().toString());
            query.setParameter("productId" + i, entry.getKey());
            query.setParameter("quantity" + i, entry.getValue());
            i++;
        }
        query.executeUpdate();
    }
}
