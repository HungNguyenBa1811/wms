package com.hung.wms.converter;

import com.hung.wms.enums.MovementType;
import com.hung.wms.model.response.stockmovement.StockMovementResponse;
import com.hung.wms.repository.entity.ProductEntity;
import com.hung.wms.repository.entity.StockMovementEntity;
import com.hung.wms.repository.entity.UserEntity;
import com.hung.wms.repository.entity.WarehouseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockMovementConverter {
    @Autowired
    private ModelMapper modelMapper;

    public StockMovementEntity toEntity(ProductEntity product, WarehouseEntity warehouse, UserEntity performedBy,
                                        MovementType movementType, Integer quantity, String reason) {
        StockMovementEntity stockMovement = new StockMovementEntity();
        stockMovement.setProduct(product);
        stockMovement.setWarehouse(warehouse);
        stockMovement.setPerformedBy(performedBy);
        stockMovement.setMovementType(movementType);
        stockMovement.setQuantity(quantity);
        stockMovement.setReason(reason);
        return stockMovement;
    }

    public StockMovementResponse toResponse(StockMovementEntity stockMovement) {
        return modelMapper.map(stockMovement, StockMovementResponse.class);
    }
}
