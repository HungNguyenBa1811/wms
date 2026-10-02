package com.hung.wms.converter;

import com.hung.wms.model.response.stockmovement.StockMovementResponse;
import com.hung.wms.repository.entity.StockMovementEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StockMovementConverter {
    @Autowired
    private ModelMapper modelMapper;

    public StockMovementResponse toResponse(StockMovementEntity stockMovement) {
        return modelMapper.map(stockMovement, StockMovementResponse.class);
    }
}
