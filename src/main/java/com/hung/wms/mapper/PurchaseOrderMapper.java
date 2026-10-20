package com.hung.wms.mapper;

import com.hung.wms.dto.request.purchaseorder.PurchaseOrderItemRequest;
import com.hung.wms.dto.request.purchaseorder.PurchaseOrderRequest;
import com.hung.wms.dto.response.purchaseorder.PurchaseOrderItemResponse;
import com.hung.wms.dto.response.purchaseorder.PurchaseOrderResponse;
import com.hung.wms.entity.PurchaseOrderEntity;
import com.hung.wms.entity.PurchaseOrderItemEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PurchaseOrderMapper {
    @Autowired
    private ModelMapper modelMapper;

    public PurchaseOrderEntity toEntity(PurchaseOrderRequest request) {
        return modelMapper.map(request, PurchaseOrderEntity.class);
    }

    public PurchaseOrderItemEntity toItemEntity(PurchaseOrderItemRequest request) {
        return modelMapper.map(request, PurchaseOrderItemEntity.class);
    }

    public PurchaseOrderResponse toResponse(PurchaseOrderEntity purchaseOrder) {
        PurchaseOrderResponse response = modelMapper.map(purchaseOrder, PurchaseOrderResponse.class);
        List<PurchaseOrderItemResponse> items = new ArrayList<>();
        for (PurchaseOrderItemEntity item : purchaseOrder.getPurchaseOrderItems()) {
            items.add(modelMapper.map(item, PurchaseOrderItemResponse.class));
        }
        response.setItems(items);
        return response;
    }
}
