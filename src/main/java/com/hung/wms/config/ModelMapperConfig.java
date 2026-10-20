package com.hung.wms.config;

import com.hung.wms.dto.request.product.ProductRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.dto.response.inventory.InventoryResponse;
import com.hung.wms.dto.response.product.ProductResponse;
import com.hung.wms.dto.response.purchaseorder.PurchaseOrderItemResponse;
import com.hung.wms.dto.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.dto.response.stockmovement.StockMovementResponse;
import com.hung.wms.entity.InventoryEntity;
import com.hung.wms.entity.ProductEntity;
import com.hung.wms.entity.PurchaseOrderEntity;
import com.hung.wms.entity.PurchaseOrderItemEntity;
import com.hung.wms.entity.StockMovementEntity;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        // categoryId is resolved to a managed CategoryEntity in the service, not by the mapper
        modelMapper.typeMap(ProductRequest.class, ProductEntity.class)
                .addMappings(m -> m.skip(ProductEntity::setCategory));
        modelMapper.typeMap(ProductEntity.class, ProductResponse.class)
                .addMapping(src -> src.getCategory().getId(), ProductResponse::setCategoryId);

        // supplierId / warehouseId / productId are resolved to managed entities in the service
        modelMapper.typeMap(PurchaseOrderRequest.class, PurchaseOrderEntity.class)
                .addMappings(m -> {
                    m.skip(PurchaseOrderEntity::setSupplier);
                    m.skip(PurchaseOrderEntity::setWarehouse);
                    m.skip(PurchaseOrderEntity::setPurchaseOrderItems);
                });
        modelMapper.typeMap(PurchaseOrderItemRequest.class, PurchaseOrderItemEntity.class)
                .addMappings(m -> {
                    m.skip(PurchaseOrderItemEntity::setProduct);
                    m.skip(PurchaseOrderItemEntity::setPurchaseOrder);
                });

        // items are mapped one by one in PurchaseOrderMapper
        modelMapper.typeMap(PurchaseOrderEntity.class, PurchaseOrderResponse.class)
                .addMappings(m -> {
                    m.map(src -> src.getSupplier().getId(), PurchaseOrderResponse::setSupplierId);
                    m.map(src -> src.getWarehouse().getId(), PurchaseOrderResponse::setWarehouseId);
                    m.skip(PurchaseOrderResponse::setItems);
                });
        modelMapper.typeMap(PurchaseOrderItemEntity.class, PurchaseOrderItemResponse.class)
                .addMapping(src -> src.getProduct().getId(), PurchaseOrderItemResponse::setProductId);

        modelMapper.typeMap(InventoryEntity.class, InventoryResponse.class)
                .addMappings(m -> {
                    m.map(src -> src.getWarehouse().getId(), InventoryResponse::setWarehouseId);
                    m.map(src -> src.getProduct().getId(), InventoryResponse::setProductId);
                });

        modelMapper.typeMap(StockMovementEntity.class, StockMovementResponse.class)
                .addMappings(m -> {
                    m.map(src -> src.getProduct().getId(), StockMovementResponse::setProductId);
                    m.map(src -> src.getWarehouse().getId(), StockMovementResponse::setWarehouseId);
                    m.map(src -> src.getPerformedBy().getId(), StockMovementResponse::setPerformedBy);
                });

        return modelMapper;
    }
}
