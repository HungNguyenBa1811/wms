package com.hung.wms.service.impl;

import com.hung.wms.converter.PurchaseOrderConverter;
import com.hung.wms.enums.MovementType;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.InvalidStateException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.model.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.model.request.purchaseorder.ReceivePurchaseOrderRequest;
import com.hung.wms.model.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.StockMovementRepository;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.UserRepository;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.repository.entity.*;
import com.hung.wms.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private PurchaseOrderConverter purchaseOrderConverter;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        if (request.getSupplierId() == null)
            throw new BadRequestException("supplierId is required");
        if (request.getWarehouseId() == null)
            throw new BadRequestException("warehouseId is required");

        PurchaseOrderEntity purchaseOrder = purchaseOrderConverter.toEntity(request);
        SupplierEntity supplier = supplierRepository
                .findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        WarehouseEntity warehouse = warehouseRepository
                .findByIdAndIsDeletedFalse(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setWarehouse(warehouse);
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        if (request.getItems() != null) {
            for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
                addItem(purchaseOrder, itemRequest);
            }
        }
        return purchaseOrderConverter.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse addPurchaseOrderItem(String id, PurchaseOrderItemRequest request) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING)
            throw new InvalidStateException("Cannot add item to purchase order in status: " + purchaseOrder.getStatus());
        addItem(purchaseOrder, request);
        return purchaseOrderConverter.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(String id, ReceivePurchaseOrderRequest request) {
        if (request.getReceivedBy() == null)
            throw new BadRequestException("receivedBy is required");

        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING)
            throw new InvalidStateException("Cannot receive purchase order in status: " + purchaseOrder.getStatus());
        if (purchaseOrder.getPurchaseOrderItems().isEmpty())
            throw new BadRequestException("Cannot receive purchase order without items");
        UserEntity receivedBy = userRepository
                .findById(request.getReceivedBy())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getReceivedBy()));

        WarehouseEntity warehouse = purchaseOrder.getWarehouse();
        for (PurchaseOrderItemEntity item : purchaseOrder.getPurchaseOrderItems()) {
            ProductEntity product = item.getProduct();

            InventoryEntity inventory = inventoryRepository
                    .findByWarehouseIdAndProductIdForUpdate(warehouse.getId(), product.getId())
                    .orElseGet(() -> newInventory(warehouse, product));
            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventoryRepository.save(inventory);

            StockMovementEntity stockMovement = new StockMovementEntity();
            stockMovement.setProduct(product);
            stockMovement.setWarehouse(warehouse);
            stockMovement.setPerformedBy(receivedBy);
            stockMovement.setMovementType(MovementType.IN);
            stockMovement.setQuantity(item.getQuantity());
            stockMovement.setReason("Receive purchase order " + purchaseOrder.getId());
            stockMovementRepository.save(stockMovement);
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        return purchaseOrderConverter.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    public List<PurchaseOrderResponse> findAllPurchaseOrders() {
        List<PurchaseOrderEntity> purchaseOrderEntityList = purchaseOrderRepository.findAll();
        List<PurchaseOrderResponse> purchaseOrderResponseList = new ArrayList<>();
        for (PurchaseOrderEntity items : purchaseOrderEntityList) {
            purchaseOrderResponseList.add(purchaseOrderConverter.toResponse(items));
        }
        return purchaseOrderResponseList;
    }

    @Override
    public PurchaseOrderResponse findPurchaseOrderById(String id) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return purchaseOrderConverter.toResponse(purchaseOrder);
    }

    private void addItem(PurchaseOrderEntity purchaseOrder, PurchaseOrderItemRequest request) {
        if (request.getProductId() == null)
            throw new BadRequestException("productId is required");
        if (request.getQuantity() == null || request.getQuantity() <= 0)
            throw new BadRequestException("quantity must be greater than 0");
        if (request.getUnitCost() == null || request.getUnitCost().compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestException("unitCost must not be negative");

        PurchaseOrderItemEntity item = purchaseOrderConverter.toItemEntity(request);
        ProductEntity product = productRepository
                .findByIdAndIsDeletedFalse(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        item.setProduct(product);
        item.setPurchaseOrder(purchaseOrder);
        purchaseOrder.getPurchaseOrderItems().add(item);
    }

    private InventoryEntity newInventory(WarehouseEntity warehouse, ProductEntity product) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setWarehouse(warehouse);
        inventory.setProduct(product);
        inventory.setQuantity(0);
        return inventory;
    }
}
