package com.hung.wms.service.impl;

import com.hung.wms.mapper.PurchaseOrderMapper;
import com.hung.wms.mapper.StockMovementMapper;
import com.hung.wms.enums.MovementType;
import com.hung.wms.enums.PurchaseOrderStatus;
import com.hung.wms.exception.BadRequestException;
import com.hung.wms.exception.InvalidStateException;
import com.hung.wms.exception.ResourceNotFoundException;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderSearchRequest;
import com.hung.wms.dto.response.common.PageResponse;
import com.hung.wms.dto.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.repository.InventoryRepository;
import com.hung.wms.repository.ProductRepository;
import com.hung.wms.repository.PurchaseOrderRepository;
import com.hung.wms.repository.StockMovementRepository;
import com.hung.wms.repository.SupplierRepository;
import com.hung.wms.repository.UserRepository;
import com.hung.wms.repository.WarehouseRepository;
import com.hung.wms.entity.*;
import com.hung.wms.repository.specification.PurchaseOrderSpecification;
import com.hung.wms.service.PurchaseOrderService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private StockMovementMapper stockMovementMapper;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(PurchaseOrderRequest request) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderMapper.toEntity(request);
        SupplierEntity supplier = supplierRepository
                .findByIdAndIsDeletedFalse(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + request.getSupplierId()));
        WarehouseEntity warehouse = warehouseRepository
                .findByIdAndIsDeletedFalse(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + request.getWarehouseId()));
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setWarehouse(warehouse);
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        Map<String, ProductEntity> products = lockProducts(request.getItems());
        for (PurchaseOrderItemRequest itemRequest : request.getItems()) {
            addItem(purchaseOrder, itemRequest, () -> Optional.ofNullable(products.get(itemRequest.getProductId())));
        }
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse addPurchaseOrderItem(String id, PurchaseOrderItemRequest request) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING)
            throw new InvalidStateException("Cannot add item to purchase order in status: " + purchaseOrder.getStatus());
        addItem(purchaseOrder, request,
                () -> productRepository.findByIdAndIsDeletedFalseForUpdate(request.getProductId()));
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional
    public PurchaseOrderResponse receivePurchaseOrder(String id, Principal principal) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository
                .findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING)
            throw new InvalidStateException("Cannot receive purchase order in status: " + purchaseOrder.getStatus());
        if (purchaseOrder.getPurchaseOrderItems().isEmpty())
            throw new BadRequestException("Cannot receive purchase order without items");
        String currentUsername = principal.getName();
        UserEntity receivedBy = userRepository
                .findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));
        WarehouseEntity warehouse = purchaseOrder.getWarehouse();
        // 1 statement for all items: INSERT ... ON DUPLICATE KEY UPDATE adds to the existing row or creates it.
        // Atomic in MySQL, so two receives creating the same (warehouse, product) row at the same time no longer
        // fail on the unique key, and two receives adding to the same row cannot lose an update.
        inventoryRepository.addQuantities(warehouse.getId(), quantityByProduct(purchaseOrder.getPurchaseOrderItems()));
        // No SELECT inside this loop: the stock movement INSERTs wait until commit and are sent as 1 JDBC batch
        for (PurchaseOrderItemEntity item : purchaseOrder.getPurchaseOrderItems()) {
            stockMovementRepository.save(stockMovementMapper.toEntity(
                    item.getProduct(), warehouse, receivedBy, MovementType.IN, item.getQuantity(),
                    "Receive purchase order " + purchaseOrder.getId()));
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrder.setReceivedAt(LocalDateTime.now());
        purchaseOrder.setReceivedBy(receivedBy);
        return purchaseOrderMapper.toResponse(purchaseOrderRepository.save(purchaseOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> findAllPurchaseOrders(PurchaseOrderSearchRequest request, Pageable pageable) {
        Page<PurchaseOrderEntity> purchaseOrderEntityPage = purchaseOrderRepository
                .findAll(PurchaseOrderSpecification.search(request), pageable);
        return new PageResponse<>(purchaseOrderEntityPage.map(purchaseOrderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse findPurchaseOrderById(String id) {
        PurchaseOrderEntity purchaseOrder = purchaseOrderRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found with id: " + id));
        return purchaseOrderMapper.toResponse(purchaseOrder);
    }

    // productLookup is only called after the duplicate check, so a duplicate still fails with 400 before any 404
    private void addItem(PurchaseOrderEntity purchaseOrder, PurchaseOrderItemRequest request,
                         Supplier<Optional<ProductEntity>> productLookup) {
        if (purchaseOrder.getPurchaseOrderItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(request.getProductId())))
            throw new BadRequestException("Product already exists in this purchase order");

        PurchaseOrderItemEntity item = purchaseOrderMapper.toItemEntity(request);
        ProductEntity product = productLookup.get()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));
        item.setProduct(product);
        item.setPurchaseOrder(purchaseOrder);
        purchaseOrder.getPurchaseOrderItems().add(item);
    }

    // 1 SELECT ... FOR UPDATE for all products of the request. A missing / soft-deleted product is simply
    // absent from the map, addItem then throws the same 404 as before.
    private Map<String, ProductEntity> lockProducts(List<PurchaseOrderItemRequest> items) {
        Set<String> productIds = items.stream()
                .map(PurchaseOrderItemRequest::getProductId)
                .collect(Collectors.toSet());
        if (productIds.isEmpty()) return Map.of();
        return productRepository.findAllByIdInAndIsDeletedFalseForUpdate(productIds).stream()
                .collect(Collectors.toMap(ProductEntity::getId, Function.identity()));
    }

    // Product id -> quantity to add. A PO never has the same product twice (addItem rejects it), Integer::sum is just a safe merge
    private Map<String, Integer> quantityByProduct(List<PurchaseOrderItemEntity> items) {
        return items.stream()
                .collect(Collectors.toMap(item -> item.getProduct().getId(), PurchaseOrderItemEntity::getQuantity, Integer::sum));
    }
}
